package ba.sake.sbt.build.extract

import sbt.*
import sbt.Keys.*

object BuildStructureExportPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val exportBuildStructure = taskKey[Unit]("Exports all projects in JSON format with dependencies and compiler options")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    exportBuildStructure := Def.taskDyn {
      val log = streams.value.log
      val stateValue = state.value
      val project = thisProject.value
      val testProject = thisProject.in(Test).value
      val projectRef = thisProjectRef.value
      val structure = Project.structure(stateValue)
      val structureData = structure.data
      log.info(s"Extracting information for project ${project.id}...")

      Def.task {
        val interProjectDependencies = project.referenced.map { dep =>
          InterProjectDependencyExport(
            project = dep.project,
            configuration = dep.configuration.getOrElse("default")
          )
        }
        // main project
        val externalDependencies = libraryDependencies.in(projectRef, Compile).get(structureData).getOrElse(Seq.empty).map { dep =>
          DependencyExport(
            name = dep.name,
            organization = dep.organization,
            revision = dep.revision,
            crossVersion = dep.crossVersion match {
              case _: CrossVersion.Binary => "binary"
              case _: CrossVersion.Full => "full"
              case CrossVersion.Disabled => "none"
              case _ => "unknown"
            }
          )
        }
        val javacOptionsValue = javacOptions.in(projectRef, Compile).get(structureData).get.value
        val scalacOptionsValue = scalacOptions.in(projectRef, Compile).get(structureData).get.value
        val mainProjectExport = ProjectExport(
          id = project.id,
          base = project.base.getAbsolutePath,
          name = name.in(projectRef, Compile).get(structureData).getOrElse(project.id),
          scalaVersion = scalaVersion.in(projectRef, Compile).get(structureData),
          organization = organization.in(projectRef, Compile).get(structureData),
          version = version.in(projectRef, Compile).get(structureData),
          //  publishTo = publishTo.in(projectRef).get(structureData).map(_.), // TODO
          homepage = homepage.in(projectRef, Compile).get(structureData).flatten.map(_.toString),
          externalDependencies = externalDependencies,
          interProjectDependencies = interProjectDependencies,
          javacOptions = javacOptionsValue,
          scalacOptions = scalacOptionsValue
        )

        // test project
        val testExternalDependencies = libraryDependencies.in(projectRef, Test).get(structureData).getOrElse(Seq.empty).map { dep =>
          DependencyExport(
            name = dep.name,
            organization = dep.organization,
            revision = dep.revision,
            crossVersion = dep.crossVersion match {
              case _: CrossVersion.Binary => "binary"
              case _: CrossVersion.Full => "full"
              case CrossVersion.Disabled => "none"
              case _ => "unknown"
            }
          )
        }
        val testJavacOptionsValue = javacOptions.in(projectRef, Test).get(structureData).get.value
        val testScalacOptionsValue = scalacOptions.in(projectRef, Test).get(structureData).get.value
        val testProjectExport = ProjectExport(
          id = testProject.id + "-test",
          base = testProject.base.getAbsolutePath,
          name = name.in(projectRef, Test).get(structureData).getOrElse(testProject.id),
          scalaVersion = scalaVersion.in(projectRef, Test).get(structureData),
          organization = organization.in(projectRef, Test).get(structureData),
          version = version.in(projectRef, Test).get(structureData),
          //  publishTo = publishTo.in(projectRef).get(structureData).map(_.), // TODO
          homepage = homepage.in(projectRef, Test).get(structureData).flatten.map(_.toString),
          externalDependencies = testExternalDependencies,
          interProjectDependencies = interProjectDependencies,
          javacOptions = testJavacOptionsValue,
          scalacOptions = testScalacOptionsValue
        )

        val projectExports = Seq(mainProjectExport, testProjectExport)
        val res = upickle.default.write(BuildStructureExport(projectExports))
        sbt.IO.write(file(s"build-export/${mainProjectExport.id}.json"), res)
      }
    }.value
  )
}
