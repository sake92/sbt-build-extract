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
      //val stateValue = state.value
      //val structure = Project.structure(stateValue)
      //val structureData = structure.data
      log.info(s"Extracting information for project ${project.id}...")
      
      Def.task {
        val interProjectDependencies = project.referenced.map { dep =>
          InterProjectDependencyExport(
            project = dep.project,
            configuration = dep.configuration.getOrElse("default")
          )
        }
        val repositories = (projectRef / resolvers).value
          .collect { case mr: MavenRepository =>
            mr.root
          }
        val artifactValue = (projectRef / artifact).value
        val descriptionValue = (projectRef / description).value
        val developerExports = (projectRef / developers).value.map { d =>
          DeveloperExport(id = d.id, name = d.name, email = d.email, url = d.url.toString)
        }
        val licenseExports = (projectRef / licenses).value.map { case (name, url) =>
          LicenseExport(name = name, url = url.toString)
        }
        val scmInfoExport = (projectRef / scmInfo).value.map { info =>
          ScmInfoExport(
            browseUrl = info.browseUrl.toString,
            connection = info.connection,
            devConnection = info.devConnection
          )
        }

        // main project
        val externalDependencies = {
          (projectRef / libraryDependencies).value.map { dep =>
            val excludes = dep.exclusions.map { excl =>
              DependencyExcludeExport(organization = excl.organization, name = excl.name)
            }
            DependencyExport(
              name = dep.name,
              organization = dep.organization,
              revision = dep.revision,
              crossVersion = dep.crossVersion match {
                case _: CrossVersion.Binary => "binary"
                case _: CrossVersion.Full   => "full"
                case _: CrossVersion.Constant   => "constant"
                case _: CrossVersion.Patch   => "patch"
                case CrossVersion.Disabled  => "none"
                case _                      => "unknown"
              },
              extraAttributes = dep.extraAttributes,
              excludes = excludes,
              configurations = dep.configurations
            )
          }
        }
        
        val projectExport = ProjectExport(
          id = project.id,
          base = project.base.getAbsolutePath,
          name = (projectRef / name).value,
          javacOptions = (projectRef / javacOptions).value,
          scalaVersion = (projectRef / scalaVersion).value,
          crossScalaVersions = (projectRef / crossScalaVersions).value,
          scalacOptions = (projectRef / scalacOptions).value,
          interProjectDependencies = interProjectDependencies,
          externalDependencies = externalDependencies,
          repositories = repositories,
          // for some reason we cant do (projectRef / Compile / resourceDirectories) .. throws
          sourceDirs = (Compile / sourceDirectories).value.map(_.getAbsolutePath),
          testSourceDirs = (Test / sourceDirectories).value.map(_.getAbsolutePath),
          resourceDirs = (Compile / resourceDirectories).value.map(_.getAbsolutePath),
          testResourceDirs = (Test / resourceDirectories).value.map(_.getAbsolutePath),
          plugins = project.autoPlugins.map(_.label),
          // publish stuff
          organization = (projectRef / organization).value,
          artifactName = artifactValue.name,
          artifactType = artifactValue.`type`,
          artifactClassifier = artifactValue.classifier,
          version = (projectRef / version).value,
          description = descriptionValue,
          homepage = (projectRef / homepage).value.map(_.toString),
          developers = developerExports,
          licenses = licenseExports,
          scmInfo = scmInfoExport
        )

        val res = upickle.default.write(projectExport)
        sbt.IO.write(file(s"target/build-export/${projectExport.id}.json"), res)
      }
    }.value
  )
  
}
