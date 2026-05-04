# Fix platformOpt Detection + Integration Test Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the broken `platformOpt` detection (name-based heuristic that never fires for `%%%` deps) by inspecting `CrossVersion.prefix` instead, and add a scripted integration test with a cross-platform project to verify correctness.

**Architecture:** Replace `dep.name.contains("_sjs")` with pattern matching on `dep.crossVersion` to read the `prefix` field of `CrossVersion.Binary`/`CrossVersion.Full`. Add an sbt scripted test (`src/sbt-test/sbt-build-extract/cross-platform/`) using sbt-crossproject that declares JVM/JS/Native deps and validates the exported JSON for each platform.

**Tech Stack:** sbt 1.12.0, sbt-crossproject 1.3.2, sbt-scalajs 1.18.2, sbt-scala-native 0.5.7

---

### Task 1: Fix platformOpt Detection in Plugin

**Files:**
- Modify: `plugin/src/main/scala/ba/sake/sbt/build/extract/BuildStructureExportPlugin.scala:71-75`

- [ ] **Step 1: Replace the broken name-based heuristic with crossVersion.prefix inspection**

Change lines 71-75 from:
```scala
platformOpt = {
    if (dep.name.contains("_sjs")) Some("ScalaJS")
    else if (dep.name.contains("_native")) Some("ScalaNative")
    else None
},
```

To:
```scala
platformOpt = dep.crossVersion match {
    case b: CrossVersion.Binary if b.prefix.contains("_sjs")    => Some("ScalaJS")
    case b: CrossVersion.Binary if b.prefix.contains("_native") => Some("ScalaNative")
    case f: CrossVersion.Full   if f.prefix.contains("_sjs")    => Some("ScalaJS")
    case f: CrossVersion.Full   if f.prefix.contains("_native") => Some("ScalaNative")
    case _ => None
},
```

- [ ] **Step 2: Verify it compiles**

Run: `sbt plugin/compile`
Expected: Compilation succeeds.

- [ ] **Step 3: Commit**

```bash
git add plugin/src/main/scala/ba/sake/sbt/build/extract/BuildStructureExportPlugin.scala
git commit -m "fix: detect platformOpt from CrossVersion.prefix instead of dep.name"
```

---

### Task 2: Set Up Scripted Test Infrastructure

**Files:**
- Modify: `build.sbt` (root)
- Create: `src/sbt-test/sbt-build-extract/cross-platform/project/build.properties`
- Create: `src/sbt-test/sbt-build-extract/cross-platform/project/plugins.sbt`
- Create: `src/sbt-test/sbt-build-extract/cross-platform/build.sbt`
- Create: `src/sbt-test/sbt-build-extract/cross-platform/test`

- [ ] **Step 1: Enable ScriptedPlugin in build.sbt**

In the root `build.sbt`, change the plugin project from:
```scala
lazy val plugin = (project in file("plugin"))
  .settings(
    name := "sbt-build-extract",
    description := "Sbt plugin for extracting build information",
    sbtPlugin := true
  )
  .dependsOn(core)
```

To:
```scala
lazy val plugin = (project in file("plugin"))
  .enablePlugins(ScriptedPlugin)
  .settings(
    name := "sbt-build-extract",
    description := "Sbt plugin for extracting build information",
    sbtPlugin := true,
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )
  .dependsOn(core)
```

- [ ] **Step 2: Create test project's build.properties**

Create `src/sbt-test/sbt-build-extract/cross-platform/project/build.properties`:
```
sbt.version=1.12.0
```

- [ ] **Step 3: Create test project's plugins.sbt**

Create `src/sbt-test/sbt-build-extract/cross-platform/project/plugins.sbt`:
```scala
addSbtPlugin("ba.sake" % "sbt-build-extract" % sys.props("plugin.version"))

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.18.2")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"         % "0.5.7")
```

- [ ] **Step 4: Create test project's build.sbt**

Create `src/sbt-test/sbt-build-extract/cross-platform/build.sbt`:
```scala
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val foo = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .settings(
    scalaVersion := "3.3.5",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "4.1.0"
  )
  .jvmSettings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0"
  )
  .nativeSettings(
    libraryDependencies += "com.lihaoyi" %%% "fansi" % "0.5.0"
  )
```

- [ ] **Step 5: Create test script**

Create `src/sbt-test/sbt-build-extract/cross-platform/test`:
```
# Run the export task across all projects
> exportAllBuildStructures

# Verify all three JSON files exist
$ exists target/build-export/fooJVM.json
$ exists target/build-export/fooJS.json
$ exists target/build-export/fooNative.json

# JVM project: must NOT contain any platform markers
- exec grep -q 'ScalaJS\|ScalaNative' target/build-export/fooJVM.json

# JS project: must contain ScalaJS platform marker
$ exec grep -q '"platformOpt":"ScalaJS"' target/build-export/fooJS.json

# Native project: must contain ScalaNative platform marker
$ exec grep -q '"platformOpt":"ScalaNative"' target/build-export/fooNative.json
```

(Note: `- exec` expects the command to **fail** — i.e., grep must NOT find `ScalaJS` or `ScalaNative` in fooJVM.json.)

- [ ] **Step 6: Run scripted test to verify it passes**

Run: `sbt plugin/scripted`
Expected: All tests pass, including `cross-platform`.

- [ ] **Step 7: Commit**

```bash
git add build.sbt src/sbt-test/
git commit -m "test: add scripted integration test for cross-platform dependency platformOpt"
```

---

### Task 3: Add Scripted Tests to CI

**Files:**
- Modify: `.github/workflows/ci.yml`

- [ ] **Step 1: Add scripted test step to CI workflow**

In `.github/workflows/ci.yml`, add after the existing `sbt test` step (line 20):
```yaml
      - run: sbt test
      - run: sbt plugin/scripted
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/ci.yml
git commit -m "ci: add scripted integration tests to CI pipeline"
```
