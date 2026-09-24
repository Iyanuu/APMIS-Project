import tempfile
import unittest
from pathlib import Path

from agent.core import build_project_graph, evaluate_change_impact, risk_meets_or_exceeds


PARENT_POM = """<project>
  <modules>
    <module>../mod-a</module>
    <module>../mod-b</module>
  </modules>
</project>
"""

MOD_A_POM = """<project>
  <artifactId>sormas-api</artifactId>
</project>
"""

MOD_B_POM = """<project>
  <artifactId>apmis-flow</artifactId>
  <dependencies>
    <dependency>
      <groupId>de.symeda.sormas</groupId>
      <artifactId>sormas-api</artifactId>
    </dependency>
  </dependencies>
</project>
"""


class CoreTests(unittest.TestCase):
    def test_graph_and_impact(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            base = root / "sormas-base"
            mod_a = root / "mod-a"
            mod_b = root / "mod-b"
            base.mkdir(parents=True)
            mod_a.mkdir(parents=True)
            mod_b.mkdir(parents=True)

            (base / "pom.xml").write_text(PARENT_POM, encoding="utf-8")
            (mod_a / "pom.xml").write_text(MOD_A_POM, encoding="utf-8")
            (mod_b / "pom.xml").write_text(MOD_B_POM, encoding="utf-8")

            graph = build_project_graph(base / "pom.xml")
            self.assertIn("sormas-api", graph.modules_by_artifact)
            self.assertIn("apmis-flow", graph.modules_by_artifact)
            self.assertIn("apmis-flow", graph.reverse_deps["sormas-api"])

            changed = [mod_a / "src" / "main" / "java" / "Foo.java"]
            report = evaluate_change_impact(changed, graph)
            self.assertEqual(report["risk"], "high")
            self.assertTrue(any(s["title"] == "Public API contract change" for s in report["scenarios"]))

    def test_external_deploy_script_rules(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            base = root / "sormas-base"
            mod_a = root / "mod-a"
            mod_b = root / "mod-b"
            deploy = root / "deploy"
            base.mkdir(parents=True)
            mod_a.mkdir(parents=True)
            mod_b.mkdir(parents=True)
            deploy.mkdir(parents=True)

            (base / "pom.xml").write_text(PARENT_POM, encoding="utf-8")
            (mod_a / "pom.xml").write_text(MOD_A_POM, encoding="utf-8")
            (mod_b / "pom.xml").write_text(MOD_B_POM, encoding="utf-8")
            (deploy / "server-update.sh").write_text("Linuxx\nABOUT_FILES_DIR\n", encoding="utf-8")

            graph = build_project_graph(base / "pom.xml")
            report = evaluate_change_impact([deploy / "server-update.sh"], graph)

            titles = {scenario["title"] for scenario in report["scenarios"]}
            self.assertIn("JDK 17 runtime flag gap", titles)
            self.assertIn("OS detection typo in update flow", titles)
            self.assertIn("Variable expansion bug risk", titles)
            self.assertEqual(report["risk"], "high")

    def test_risk_threshold_helper(self):
        self.assertTrue(risk_meets_or_exceeds("high", "medium"))
        self.assertTrue(risk_meets_or_exceeds("medium", "medium"))
        self.assertFalse(risk_meets_or_exceeds("low", "medium"))


if __name__ == "__main__":
    unittest.main()
