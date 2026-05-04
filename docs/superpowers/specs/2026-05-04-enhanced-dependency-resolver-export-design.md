# Enhanced Dependency Export

**Date:** 2026-05-04
**Status:** Implemented

## Changes

### 1. `DependencyExport` — add 5 new fields + enum-ify `crossVersion`

- `platformOpt: Option[String]` — ScalaJS/ScalaNative platform (hardcoded to `None` until sbt librarymanagement adds the field)
- `isChanging: Boolean` — snapshot/changing dep flag
- `isTransitive: Boolean` — transitive dep control
- `isForce: Boolean` — version override flag
- `branchName: Option[String]` — git branch dep
- `crossVersion` changed from `String` to `CrossVersionExport` enum

### 2. New enum: `CrossVersionExport`

Sealed trait with case objects, serialized as lowercase strings matching previous output:

- `Binary` → `"binary"`
- `Full` → `"full"`
- `Constant` → `"constant"`
- `Patch` → `"patch"`
- `Disabled` → `"none"`
- `Unknown` → `"unknown"`

## Files changed

| File | Change |
|------|--------|
| `core/.../BuildStructureExport.scala` | Add `CrossVersionExport` enum; add 5 fields to `DependencyExport`; change `crossVersion` type |
| `plugin/.../BuildStructureExportPlugin.scala` | Update dep mapping with new fields + `crossVersionExport` method |
| `README.md` | Update JSON example |

## Non-goals

- No resolver type changes (URLs sufficient)
- No build-level settings
- No credential export
