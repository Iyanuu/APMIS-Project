# APMIS — working fork

Working fork of [AFG-Polio-Data/APMIS-Project](https://github.com/AFG-Polio-Data/APMIS-Project), used for the engagement to raise application quality through tests and a better developer workflow.

APMIS is derived from [SORMAS](https://github.com/hzi-braunschweig/SORMAS-Project). The upstream SORMAS readme — module descriptions, server setup, contribution guides — is preserved at [`apmis-readme.md`](apmis-readme.md) and is still the reference for anything this file does not cover.

## Where work is tracked

Issues on this repository, grouped on the **APMIS Testing** project board.

| Label | Meaning |
|---|---|
| `phase-1` | Turn the lights on — make the build and its signal real |
| `phase-2` | Protect the field — contract tests between modules |
| `wiring` | Pipeline plumbing, done before adding tests |
| `good first task` | Small, self-contained, safe to pick up cold |

Pull requests land on `development` here. Changes intended for upstream are raised separately and kept narrow.

## Building

**Java 17.** The parent pom sets `<release>17</release>`; a JDK 11 toolchain fails to compile `sormas-api`.

```bash
cd sormas-base
mvn verify          # ~7 minutes, all server modules
```

The Android app resolves `de.symeda.sormas:sormas-api` from `mavenLocal()`, so that module must be installed by Maven **before** Gradle runs:

```bash
cd sormas-base && mvn install -pl :sormas-api -am -DskipTests
cd ../sormas-app && ./gradlew :app:assembleDebug :app:testDebugUnitTest
```

## Test coverage as it stands

Honest baseline, measured on this branch:

| Module | Unit tests | Runs in CI |
|---|---|---|
| `sormas-api`, `sormas-backend`, `sormas-ui`, `sormas-rest`, `apmis-flow` | none | — |
| `sormas-app` | 3 files | yes |
| `sormas-e2e-tests` | 22 Cucumber features | not yet |

`mvn verify` reaches `failsafe:integration-test` on every module and runs nothing server-side. JUnit, Mockito, JaCoCo and Surefire are already inherited from `sormas-base/pom.xml`, so a test placed in `<module>/src/test/java/` runs with no build changes.

## Things that will catch you out

Each of these cost real time to find.

**The Android workflow's JDK is tied to the server, not the app.** `sormas_app_ci.yml` must use JDK 17 because it builds `sormas-api` with Maven first. Setting it to match the app's bytecode target breaks the build.

**Two migration counters move together.** The server schema (`sormas_schema.sql`, `INSERT INTO schema_version`, currently 498) and the app's local database (`DatabaseHelper.DATABASE_VERSION`, currently 359). Both are append-only single files, so parallel work collides on them. Rebase before merging.

**A cross-module change is atomic by necessity.** `sormas-api` is compiled against by the backend, UI, REST layer and the Android app. Adding a DTO field and deferring the app half leaves `development` uncompilable for everyone.

**A path-filtered check cannot be a required check.** `sormas_app_ci.yml` only triggers on `sormas-app/**` and `sormas-api/**`. Marking it required would leave a `sormas-ui`-only pull request waiting forever for a result that never arrives.

**Beware a build that passes for the wrong reason.** Three failures were stacked here at one point, each hiding the next: checkout failed on a missing secret, then dependency resolution failed on a withdrawn Vaadin beta, then compilation failed on a class that had never been committed. A green tick after fixing one only means the next one is now visible.

## Modules

As upstream, plus `apmis-flow` (Vaadin 23 web module, APMIS-specific). Full descriptions in [`apmis-readme.md`](apmis-readme.md#project-structure).

## Licence

GPL v3, as upstream. See [`LICENSE`](LICENSE).
