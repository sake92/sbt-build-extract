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

This will generate files inside `build-export` folder, for example:
```
build-export/myproject1.json
build-export/myproject2.json
```

Each of these files contains the build structure of a subproject in JSON format.
For example if `myproject1` depends on `myproject2`, `myproject1.json` will contain something like this:
```json
{
    "projects": [
        {
            "id": "myproject1",
            "base": "D:\\projects\\myproject\\myproject1",
            "name": "myproject1",
            "scalaVersion": "3.3.4",
            "organization": "com.example",
            "version": "0.0.1-SNAPSHOT",
            "homepage": "..",
            "externalDependencies": [
                {
                    "organization": "org.scala-lang",
                    "name": "scala3-library",
                    "revision": "3.3.4",
                    "crossVersion": "binary"
                }
            ],
            "interProjectDependencies": [
                {
                    "project": "myproject2",
                    "configuration": "default"
                }
            ],
            "javacOptions": [],
            "scalacOptions": [
                "-deprecation",
                "-Yretain-trees",
                "-Wunused:all"
            ]
        },
        {
            "id": "myproject1-test",
            "base": "D:\\projects\\myproject\\myproject1",
            "name": "hepek",
            "scalaVersion": "3.3.4",
            "organization": "com.example",
            "version": "0.0.1-SNAPSHOT",
            "homepage": "...",
            "externalDependencies": [
                {
                    "organization": "org.scala-lang",
                    "name": "scala3-library",
                    "revision": "3.3.4",
                    "crossVersion": "binary"
                }
            ],
            "interProjectDependencies": [
                {
                    "project": "myproject2",
                    "configuration": "default"
                }
            ],
            "javacOptions": [],
            "scalacOptions": [
                "-deprecation",
                "-Yretain-trees",
                "-Wunused:all"
            ]
        }
    ]
}
```

Sbt sees `myproject1` as one project with multiple axis.
But here we treat them as separate projects:
- one for main (Compile) project
- one for test project

