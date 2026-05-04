# AGENTS.md

## Architecture

Two sbt subprojects:
- **`core/`** — library (`sbt-build-extract-core`), cross-published for 2.12/2.13/3. Holds the data model case classes with `upickle` JSON serialization.
- **`plugin/`** — sbt plugin (`sbt-build-extract`), compiled for 2.12 only (`sbtPlugin := true`). Depends on `core`. The `AutoPlugin` that reads sbt settings and writes JSON to `target/build-export/<id>.json`.

Root `build.sbt` sets `ThisBuild / scalaVersion := scala212` (2.12.18).

## Development commands

```bash
sbt compile                    # compile all projects
sbt plugin/compile             # compile just the plugin
sbt test                       # unit tests (none currently)
sbt plugin/scripted            # run all scripted integration tests
sbt "plugin/scripted sbt-build-extract/cross-platform"  # run a single scripted test
```

## Scripted tests

Live under `plugin/src/sbt-test/sbt-build-extract/` (because `ScriptedPlugin` is enabled on the plugin subproject — NOT under the root `src/sbt-test/`).

The `test` script file uses sbt's scripted DSL:
- `> taskName` — run an sbt task
- `$ exists <file>` — assert file exists
- `$ exec <shell cmd>` — assert command succeeds (exit 0)
- **Negation:** Use `$ exec sh -c '! ...'` — the `- exec` syntax is NOT supported

## Data model conventions (core)

- Sealed trait enums use the `readwriter[String].bimap` pattern for JSON serialization (see `CrossVersionExport`, `PlatformExport`).
- Case classes use `implicit val rw: ReadWriter[T] = macroRW`.
- `platformOpt` is `Option[PlatformExport]` — serializes as `null`, `"ScalaJS"`, or `"ScalaNative"`.

## Gotchas

### Inter-project dependencies: use `project.dependencies`, NOT `project.referenced`

`sbt.ProjectDefinition.referenced` calls `dependencies.map(_.project)` which strips the `configuration` field. Use `project.dependencies` to get `ResolvedClasspathDependency` objects that retain `configuration: Option[String]`.

When iterating `project.dependencies`, `dep.project` is a `ProjectRef` (not a String). Use `dep.project.project` to get the project ID string.

### platformOpt: detect from `CrossVersion.prefix`, NOT `dep.name`

The `platformOpt` heuristic checks the `prefix` and `suffix` fields of `CrossVersion.Binary`/`CrossVersion.Full`:

| Dependency | CrossVersion | prefix |
|---|---|---|
| JVM `%%` | `Binary("", "")` | empty |
| ScalaJS `%%%` | `Binary("_sjs1", "_3")` | contains `sjs` |
| ScalaNative `%%%` | `Binary("_native0.4", "_3")` | contains `native` |

Do NOT check `dep.name` — `ModuleID.name` is the bare artifact name and never contains platform suffixes.

## CI

- **CI** (`.github/workflows/ci.yml`): runs `sbt test` + `sbt plugin/scripted` on push to main and PRs.
- **Release** (`.github/workflows/release.yml`): runs `sbt ci-release` on tag push. Requires Sonatype + PGP secrets (managed by `sbt-ci-release` plugin).
