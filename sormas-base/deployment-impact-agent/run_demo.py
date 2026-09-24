from pathlib import Path

from agent.core import build_project_graph, evaluate_change_impact, render_markdown_report


def main() -> int:
    parent_pom = (Path(__file__).resolve().parent.parent / "pom.xml").resolve()
    graph = build_project_graph(parent_pom)
    repo_root = parent_pom.parent.parent

    sample_changed = [
        (repo_root / "sormas-api" / "src" / "main" / "java" / "de" / "symeda" / "sormas" / "api" / "devicemanager" / "DeviceManagerFacade.java").resolve(),
        (repo_root / "deploy" / "server-update.sh").resolve(),
    ]

    report = evaluate_change_impact(sample_changed, graph)
    print(render_markdown_report(report))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
