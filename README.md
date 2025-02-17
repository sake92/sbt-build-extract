# sbt-build-extract

Extracts build structure of sbt projects.

## Usage

Add to your `project/plugins.sbt`:
```scala
addSbtPlugin("ba.sake" % "sbt-build-extract" % "0.1.0-SNAPSHOT")
libraryDependencies += "ba.sake" %% "sbt-build-extract-core" % "0.1.0-SNAPSHOT"
```

then run
```bash
sbt> exportBuildStructure
```

This will generate files inside `target/build-export` folder, for example:
```
myproject1.json
myproject2.json
```

Each of these files contains the build structure of a subproject in JSON format.
For example if `myproject1` depends on `myproject2`, the `myproject1.json` file will contain something like this:
```json
{
    "artifactClassifier": null,
    "artifactName": "hepek",
    "artifactType": "jar",
    "base": "/projects/hepek",
    "description": "Hepek SSG",
    "developers": [
        {
          "email": "sakib@sake.ba",
          "id": "sake92",
          "name": "Sakib Hadžiavdić",
          "url": "https://sake.ba"
        }
    ],
    "externalDependencies": [
      {
        "organization": "org.scala-lang",
        "name": "scala3-library",
        "revision": "3.3.4",
        "extraAttributes": {},
        "configurations": null,
        "excludes": [],
        "crossVersion": "binary"
      },
      {
        "organization": "org.scalameta",
        "name": "munit",
        "revision": "1.0.2",
        "extraAttributes": {},
        "configurations": "test",
        "excludes": [],
        "crossVersion": "binary"
      }
    ],
    "homepage": "https://sake92.github.io/hepek",
    "id": "hepekSSG",
    "interProjectDependencies": [
        {
            "project": "myproject2",
            "configuration": "default"
        }
    ],
    "javacOptions": [
    ],
    "licenses": [
        {
          "name": "Apache-2.0",
          "url": "http://www.apache.org/licenses/LICENSE-2.0"
        }
    ],
    "name": "hepek",
    "organization": "ba.sake",
    "repositories": [
      "https://oss.sonatype.org/content/repositories/snapshots"
    ],
    "scalacOptions": [
        "-deprecation",
        "-Yretain-trees",
        "-Wunused:all"
    ],
    "scalaVersion": "3.3.4",
    "scmInfo": {
        "browseUrl": "https://github.com/sake92/hepek",
        "connection": "scm:git:git@github.com:sake92/hepek.git",
        "devConnection": null
    },
    "version": "0.0.1-SNAPSHOT"
}
```

Sbt sees `myproject1` as one project with multiple axis.
But here we treat them as separate projects:
- one for main (Compile) project
- one for test project

