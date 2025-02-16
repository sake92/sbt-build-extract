package ba.sake.sbt.build.extract

import sbt.*
import sbt.Keys.*

object BuildStructureExportPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val exportBuildStructure =
      taskKey[Unit]("Exports all projects in JSON format with dependencies and compiler options")
  }

  import autoImport.*

  override lazy val projectSettings = Seq(
    exportBuildStructure := Def.taskDyn {
      val log = streams.value.log

      val project = thisProject.value
      val projectRef = thisProjectRef.value
      // val stateValue = state.value
      // val structure = Project.structure(stateValue)
      // val structureData = structure.data
      log.info(s"Extracting information for project ${project.id}...")

      Def.task {
        val interProjectDependencies = project.referenced.map { dep =>
          InterProjectDependencyExport(
            project = dep.project,
            configuration = dep.configuration.getOrElse("default")
          )
        }
        // main project
        val externalDependencies =
          (projectRef / Compile / libraryDependencies).value.map { dep =>
            DependencyExport(
              name = dep.name,
              organization = dep.organization,
              revision = dep.revision,
              crossVersion = dep.crossVersion match {
                case _: CrossVersion.Binary => "binary"
                case _: CrossVersion.Full   => "full"
                case CrossVersion.Disabled  => "none"
                case _                      => "unknown"
              }
            )
          }
        val mainProjectExport = ProjectExport(
          scope = "compile",
          id = project.id,
          base = project.base.getAbsolutePath,
          name = (projectRef / Compile / name).value,
          scalaVersion = (projectRef / Compile / scalaVersion).value,
          organization = (projectRef / Compile / organization).value,
          version = (projectRef / Compile / version).value,
          //  publishTo = publishTo.in(projectRef).get(structureData).map(_.), // TODO
          homepage = (projectRef / Compile / homepage).value.map(_.toString),
          externalDependencies = externalDependencies,
          interProjectDependencies = interProjectDependencies,
          javacOptions = (projectRef / Compile / javacOptions).value,
          scalacOptions = (projectRef / Compile / scalacOptions).value
        )

        // test project
        val testExternalDependencies =
          (projectRef / Test / libraryDependencies).value.map { dep =>
            DependencyExport(
              name = dep.name,
              organization = dep.organization,
              revision = dep.revision,
              crossVersion = dep.crossVersion match {
                case _: CrossVersion.Binary => "binary"
                case _: CrossVersion.Full   => "full"
                case CrossVersion.Disabled  => "none"
                case _                      => "unknown"
              }
            )
          }
        val testProjectExport = ProjectExport(
          scope = "test",
          id = project.id,
          base = project.base.getAbsolutePath,
          name = (projectRef / Test / name).value,
          scalaVersion = (projectRef / Test / scalaVersion).value,
          organization = (projectRef / Test / organization).value,
          version = (projectRef / Test / version).value,
          //  publishTo = publishTo.in(projectRef).get(structureData).map(_.), // TODO
          homepage = (projectRef / Test / homepage).value.map(_.toString),
          externalDependencies = testExternalDependencies,
          interProjectDependencies = interProjectDependencies,
          javacOptions = (projectRef / Test / javacOptions).value,
          scalacOptions = (projectRef / Test / scalacOptions).value
        )

        val projectExports = Seq(mainProjectExport, testProjectExport)
        val res = upickle.default.write(BuildStructureExport(projectExports))
        sbt.IO.write(file(s"build-export/${mainProjectExport.id}.json"), res)
      }
    }.value
  )
}
