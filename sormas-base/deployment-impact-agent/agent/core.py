import json
import os
import subprocess
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Set


RISK_WEIGHTS = {"low": 1, "medium": 2, "high": 3}
REQUIRED_JAVA17_FLAGS = [
    "--add-exports=java.base/sun.net.www=ALL-UNNAMED",
    "--add-exports=java.base/sun.security.util=ALL-UNNAMED",
    "--add-opens=java.base/java.math=ALL-UNNAMED",
]


@dataclass
class ProjectGraph:
    parent_pom: Path
    modules_by_artifact: Dict[str, Path]
    reverse_deps: Dict[str, Set[str]]


def _local_name(tag: str) -> str:
    if "}" in tag:
        return tag.split("}", 1)[1]
    return tag


def _find_child(node: ET.Element, name: str) -> ET.Element:
    for child in list(node):
        if _local_name(child.tag) == name:
            return child
    return None


def _find_children(node: ET.Element, name: str) -> List[ET.Element]:
    return [child for child in list(node) if _local_name(child.tag) == name]


def parse_parent_modules(parent_pom: Path) -> List[Path]:
    tree = ET.parse(parent_pom)
    root = tree.getroot()
    modules_node = _find_child(root, "modules")
    if modules_node is None:
        return []

    parent_dir = parent_pom.parent
    module_paths: List[Path] = []
    for module in _find_children(modules_node, "module"):
        rel = (module.text or "").strip()
        if not rel:
            continue
        resolved = (parent_dir / rel).resolve()
        module_paths.append(resolved)
    return module_paths


def parse_module_pom(module_dir: Path) -> Dict[str, object]:
    pom = module_dir / "pom.xml"
    if not pom.exists():
        return {"artifact_id": module_dir.name, "deps": []}

    tree = ET.parse(pom)
    root = tree.getroot()

    artifact_id_node = _find_child(root, "artifactId")
    artifact_id = (artifact_id_node.text or module_dir.name).strip() if artifact_id_node is not None else module_dir.name

    deps: List[str] = []
    dependencies_node = _find_child(root, "dependencies")
    if dependencies_node is not None:
        for dep in _find_children(dependencies_node, "dependency"):
            group_node = _find_child(dep, "groupId")
            artifact_node = _find_child(dep, "artifactId")
            if group_node is None or artifact_node is None:
                continue
            group = (group_node.text or "").strip()
            art = (artifact_node.text or "").strip()
            if group == "de.symeda.sormas" and art:
                deps.append(art)

    return {"artifact_id": artifact_id, "deps": deps}


def build_project_graph(parent_pom: Path) -> ProjectGraph:
    module_dirs = parse_parent_modules(parent_pom)
    modules_by_artifact: Dict[str, Path] = {}
    deps_map: Dict[str, Set[str]] = {}

    for module_dir in module_dirs:
        meta = parse_module_pom(module_dir)
        artifact = str(meta["artifact_id"])
        modules_by_artifact[artifact] = module_dir
        deps_map[artifact] = set(meta["deps"])

    reverse: Dict[str, Set[str]] = {k: set() for k in modules_by_artifact}
    for consumer, deps in deps_map.items():
        for dep in deps:
            if dep in reverse:
                reverse[dep].add(consumer)

    return ProjectGraph(parent_pom=parent_pom, modules_by_artifact=modules_by_artifact, reverse_deps=reverse)


def module_for_file(path: Path, graph: ProjectGraph) -> str:
    p = path.resolve()
    for artifact, module_dir in graph.modules_by_artifact.items():
        try:
            p.relative_to(module_dir)
            return artifact
        except ValueError:
            continue
    return "external"


def transitive_dependents(artifact: str, reverse_deps: Dict[str, Set[str]]) -> Set[str]:
    seen: Set[str] = set()
    queue = [artifact]
    while queue:
        current = queue.pop(0)
        for dep in reverse_deps.get(current, set()):
            if dep not in seen:
                seen.add(dep)
                queue.append(dep)
    return seen


def risk_meets_or_exceeds(risk: str, threshold: str) -> bool:
    return RISK_WEIGHTS.get(risk, 0) >= RISK_WEIGHTS.get(threshold, 0)


def _read_text_if_exists(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except (OSError, UnicodeDecodeError):
        return ""


def _deploy_script_scenarios(files: List[str]) -> List[Dict[str, object]]:
    scenarios: List[Dict[str, object]] = []
    deploy_scripts = [Path(f) for f in files if Path(f).name in {"server-setup.sh", "server-update.sh"}]
    if not deploy_scripts:
        return scenarios

    joined_text = "\n".join(_read_text_if_exists(script) for script in deploy_scripts)
    missing_flags = [flag for flag in REQUIRED_JAVA17_FLAGS if flag not in joined_text]

    if missing_flags:
        scenarios.append(
            {
                "severity": "high",
                "title": "JDK 17 runtime flag gap",
                "source_module": "external",
                "impacted_modules": ["sormas-ear", "sormas-rest", "apmis-flow"],
                "why": "Updated deployment scripts do not include all known JDK 17 module flags required by the current stack.",
                "test_focus": [
                    "Verify AS_JAVA points to JDK 17",
                    "Restart domain and confirm startup without IllegalAccessError/InaccessibleObjectException",
                    "Validate flags: " + ", ".join(missing_flags),
                ],
            }
        )

    if "Linuxx" in joined_text:
        scenarios.append(
            {
                "severity": "medium",
                "title": "OS detection typo in update flow",
                "source_module": "external",
                "impacted_modules": ["sormas-ear", "apmis-flow"],
                "why": "A Linux OS branch typo ('Linuxx') can break conditional update logic during provisioning.",
                "test_focus": [
                    "Run update script in dry-run mode on Linux-compatible shell",
                    "Verify OS branch selection for Linux/Darwin/Windows",
                ],
            }
        )

    if "ABOUT_FILES_DIR" in joined_text and "$ABOUT_FILES_DIR" not in joined_text:
        scenarios.append(
            {
                "severity": "medium",
                "title": "Variable expansion bug risk",
                "source_module": "external",
                "impacted_modules": ["sormas-ear", "apmis-flow"],
                "why": "A missing '$' in ABOUT_FILES_DIR checks can silently skip expected files.",
                "test_focus": [
                    "Run setup/update scripts with verbose logging",
                    "Confirm About files are copied into the target directory",
                ],
            }
        )

    return scenarios


def evaluate_change_impact(changed_files: List[Path], graph: ProjectGraph) -> Dict[str, object]:
    changed_modules: Dict[str, List[str]] = {}
    scenarios: List[Dict[str, object]] = []

    for file_path in changed_files:
        artifact = module_for_file(file_path, graph)
        changed_modules.setdefault(artifact, []).append(str(file_path))

    for artifact, files in changed_modules.items():
        impacted = [] if artifact == "external" else sorted(transitive_dependents(artifact, graph.reverse_deps))

        # Rule: API contract changes have broad blast radius.
        if artifact == "sormas-api" and any("src/main/java" in f.replace("\\", "/") for f in files):
            scenarios.append(
                {
                    "severity": "high",
                    "title": "Public API contract change",
                    "source_module": artifact,
                    "impacted_modules": impacted,
                    "why": "Changes in sormas-api Java sources can break compile-time or runtime contracts in dependent modules.",
                    "test_focus": [
                        "Build sormas-api, sormas-backend, sormas-rest, apmis-flow",
                        "Smoke-test high-traffic routes that call updated facades/DTOs",
                    ],
                }
            )

        # Rule: Deployment/runtime config changes.
        if any(name.endswith("domain.xml") or name.endswith("server-setup.sh") or name.endswith("server-update.sh") for name in files):
            scenarios.append(
                {
                    "severity": "high",
                    "title": "Runtime configuration drift",
                    "source_module": artifact,
                    "impacted_modules": ["sormas-ear", "sormas-rest", "apmis-flow"],
                    "why": "Domain or deployment script changes can cause startup/deploy regressions unrelated to application code.",
                    "test_focus": [
                        "Restart domain from clean state",
                        "Deploy sormas-ear and apmis-flow",
                        "Check server.log for module-open/security provider errors",
                    ],
                }
            )

        # Rule: POM changes can change transitive dependencies.
        if any(Path(f).name == "pom.xml" for f in files):
            scenarios.append(
                {
                    "severity": "medium",
                    "title": "Dependency graph change",
                    "source_module": artifact,
                    "impacted_modules": impacted,
                    "why": "POM updates can alter compile/runtime classpath and plugin behavior.",
                    "test_focus": [
                        f"Run mvn -f {graph.parent_pom} -DskipTests validate",
                        "Run targeted module package commands for impacted modules",
                    ],
                }
            )

        # Rule: Auth/config scripts.
        if any("keycloak" in f.lower() or f.endswith("sormas.properties") for f in files):
            scenarios.append(
                {
                    "severity": "medium",
                    "title": "Authentication/identity impact",
                    "source_module": artifact,
                    "impacted_modules": ["sormas-ear", "apmis-flow"],
                    "why": "Keycloak or properties changes may break login, role mapping, or token validation.",
                    "test_focus": [
                        "Login redirect flow",
                        "Role-based navigation checks",
                    ],
                }
            )

        if artifact == "external":
            scenarios.extend(_deploy_script_scenarios(files))

    score = sum(RISK_WEIGHTS.get(s["severity"], 0) for s in scenarios)
    severities = {s["severity"] for s in scenarios}
    if "high" in severities:
        risk = "high"
    elif "medium" in severities:
        risk = "medium"
    else:
        risk = "low"

    return {
        "risk": risk,
        "changed_modules": changed_modules,
        "scenarios": scenarios,
        "summary": {
            "changed_file_count": len(changed_files),
            "scenario_count": len(scenarios),
            "score": score,
        },
    }


def render_markdown_report(report: Dict[str, object]) -> str:
    lines = [
        f"# Deployment Impact Report ({report['risk'].upper()})",
        "",
        "## Summary",
        f"- Changed files: {report['summary']['changed_file_count']}",
        f"- Scenarios: {report['summary']['scenario_count']}",
        f"- Score: {report['summary']['score']}",
        "",
        "## Changed Modules",
    ]

    for module, files in sorted(report["changed_modules"].items()):
        lines.append(f"- `{module}` ({len(files)} file(s))")

    lines.append("")
    lines.append("## Scenarios")
    if not report["scenarios"]:
        lines.append("- No explicit risk scenarios matched current rules.")
        return "\n".join(lines)

    for idx, scenario in enumerate(report["scenarios"], start=1):
        lines.extend(
            [
                "",
                f"### {idx}. {scenario['title']} [{scenario['severity'].upper()}]",
                f"- Source module: `{scenario['source_module']}`",
                f"- Why: {scenario['why']}",
                f"- Impacted modules: {', '.join(f'`{m}`' for m in scenario['impacted_modules']) if scenario['impacted_modules'] else '(none)'}",
                "- Test focus:",
            ]
        )
        for check in scenario["test_focus"]:
            lines.append(f"  - {check}")
        lines.append("")

    return "\n".join(lines).strip() + "\n"


def changed_files_from_git(repo_dir: Path, base_ref: str, head_ref: str) -> List[Path]:
    cmd = ["git", "--no-pager", "diff", "--name-only", f"{base_ref}..{head_ref}"]
    output = subprocess.check_output(cmd, cwd=repo_dir, text=True)
    files = []
    for line in output.splitlines():
        item = line.strip()
        if item:
            files.append((repo_dir / item).resolve())
    return files


def load_rules(path: Path) -> Dict[str, object]:
    if not path.exists():
        return {}
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)