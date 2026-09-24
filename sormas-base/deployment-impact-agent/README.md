# Deployment Impact Agent

A lightweight change-impact and deployment-risk analyzer for the SORMAS/APMIS multi-module workspace.

## What it does

- Parses the parent Maven reactor from `sormas-base/pom.xml`
- Builds a module dependency graph (`de.symeda.sormas` inter-module deps)
- Maps changed files to modules
- Evaluates risk scenarios (API contract changes, deployment/config drift, auth config changes)
- Adds deploy-script heuristics for Java 17 runtime flags and known script pitfalls
- Emits markdown or JSON report for PRs/CI
- Can emit both report formats in one run
- Supports CI gate behavior via risk thresholds

## Project layout

- `agent/core.py` - graph + impact logic
- `agent/cli.py` - command-line interface
- `run_demo.py` - quick local demo
- `tests/test_core.py` - unit test harness

## Quick try

```powershell
Set-Location C:\Users\Segun\git\dockub\APMIS-AI\APMIS-Project\sormas-base\deployment-impact-agent
python -m agent.cli --changed-file C:\Users\Segun\git\dockub\APMIS-AI\APMIS-Project\sormas-api\src\main\java\de\symeda\sormas\api\devicemanager\DeviceManagerFacade.java
```

### Git diff mode

```powershell
Set-Location C:\Users\Segun\git\dockub\APMIS-AI\APMIS-Project\sormas-base\deployment-impact-agent
python -m agent.cli --git-base HEAD~1 --git-head HEAD
```

### JSON output

```powershell
Set-Location C:\Users\Segun\git\dockub\APMIS-AI\APMIS-Project\sormas-base\deployment-impact-agent
python -m agent.cli --git-base HEAD~1 --format json --output report.json
```

### Emit both markdown + JSON into one folder

```powershell
Set-Location C:\Users\Segun\git\dockub\APMIS-AI\APMIS-Project\sormas-base\deployment-impact-agent
python -m agent.cli --git-base HEAD~1 --out-dir .\artifacts
```

### CI gate mode

```powershell
Set-Location C:\Users\Segun\git\dockub\APMIS-AI\APMIS-Project\sormas-base\deployment-impact-agent
python -m agent.cli --git-base origin/main --out-dir .\artifacts --fail-on-risk high
```

Exit code behavior:
- `0`: below threshold
- `2`: threshold breached

## Run demo

```powershell
Set-Location C:\Users\Segun\git\dockub\APMIS-AI\APMIS-Project\sormas-base\deployment-impact-agent
python run_demo.py
```

## Run tests

```powershell
Set-Location C:\Users\Segun\git\dockub\APMIS-AI\APMIS-Project\sormas-base\deployment-impact-agent
python -m unittest discover -s tests -v
```

## Notes

- Rules are deterministic and intentionally simple for fast iteration.
- `agent/rules.json` is included for future externalized rule packs.
- Next precision step: add Java symbol-level usage correlation.
