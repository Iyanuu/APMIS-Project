import argparse
import json
from pathlib import Path

from .core import (
    build_project_graph,
    changed_files_from_git,
    evaluate_change_impact,
    risk_meets_or_exceeds,
    render_markdown_report,
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Deployment impact and risk analyzer.")
    parser.add_argument(
        "--parent-pom",
        default=str((Path(__file__).resolve().parents[2] / "pom.xml")),
        help="Path to parent pom.xml that defines modules (default: sormas-base/pom.xml)",
    )
    parser.add_argument("--changed-file", action="append", default=[], help="Absolute or relative file path that changed")
    parser.add_argument("--changed-file-list", help="Text file containing one changed file path per line")
    parser.add_argument("--git-base", help="Git base ref to diff from (e.g. origin/main)")
    parser.add_argument("--git-head", default="HEAD", help="Git head ref (default: HEAD)")
    parser.add_argument("--output", help="Optional output file path")
    parser.add_argument("--format", choices=["markdown", "json"], default="markdown")
    parser.add_argument("--out-dir", help="Optional output directory for generated reports")
    parser.add_argument("--emit-both", action="store_true", help="Write both markdown and JSON reports")
    parser.add_argument(
        "--fail-on-risk",
        choices=["low", "medium", "high"],
        help="Return non-zero when computed risk is at or above this threshold",
    )
    return parser.parse_args()


def collect_changed_files(args: argparse.Namespace, repo_dir: Path) -> list[Path]:
    files: list[Path] = []

    for item in args.changed_file:
        files.append(Path(item).resolve())

    if args.changed_file_list:
        list_path = Path(args.changed_file_list).resolve()
        with list_path.open("r", encoding="utf-8") as handle:
            for line in handle:
                val = line.strip()
                if val:
                    files.append(Path(val).resolve())

    if args.git_base:
        files.extend(changed_files_from_git(repo_dir=repo_dir, base_ref=args.git_base, head_ref=args.git_head))

    deduped = []
    seen = set()
    for p in files:
        key = str(p)
        if key not in seen:
            deduped.append(p)
            seen.add(key)
    return deduped


def main() -> int:
    args = parse_args()

    parent_pom = Path(args.parent_pom).resolve()
    graph = build_project_graph(parent_pom)
    repo_dir = parent_pom.parent

    changed_files = collect_changed_files(args, repo_dir)
    report = evaluate_change_impact(changed_files=changed_files, graph=graph)

    markdown_text = render_markdown_report(report)
    json_text = json.dumps(report, indent=2)

    if args.format == "json":
        text = json_text
    else:
        text = markdown_text

    emitted_output = False
    if args.out_dir:
        out_dir = Path(args.out_dir).resolve()
        out_dir.mkdir(parents=True, exist_ok=True)
        (out_dir / "impact-report.md").write_text(markdown_text, encoding="utf-8")
        (out_dir / "impact-report.json").write_text(json_text, encoding="utf-8")
        emitted_output = True

    if args.emit_both and args.output:
        out = Path(args.output).resolve()
        out.parent.mkdir(parents=True, exist_ok=True)
        if out.suffix.lower() == ".json":
            out.write_text(json_text, encoding="utf-8")
            out.with_suffix(".md").write_text(markdown_text, encoding="utf-8")
        else:
            out.write_text(markdown_text, encoding="utf-8")
            out.with_suffix(".json").write_text(json_text, encoding="utf-8")
        emitted_output = True

    if args.output:
        out = Path(args.output).resolve()
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(text, encoding="utf-8")
        emitted_output = True

    if not emitted_output:
        print(text)

    if args.fail_on_risk and risk_meets_or_exceeds(report["risk"], args.fail_on_risk):
        print(f"Risk threshold breached: computed={report['risk']} threshold={args.fail_on_risk}")
        return 2

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
