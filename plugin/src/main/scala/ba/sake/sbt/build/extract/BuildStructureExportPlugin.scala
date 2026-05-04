package ba.sake.sbt.build.extract

import sbt.*
import sbt.Keys.*

object BuildStructureExportPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val exportBuildStructure =
      taskKey[Unit]("Exports the current project in JSON format with dependencies and compiler options")
    val exportAllBuildStructures =
      taskKey[Unit]("Exports all loaded projects in JSON format, regardless of aggregation")
  }

  import autoImport.*

  override lazy val buildSettings = Seq(
    exportAllBuildStructures := exportBuildStructure.all(ScopeFilter(inAnyProject)).value
  )

  override lazy val projectSettings = Seq(
    exportBuildStructure := Def.taskDyn {
      val log = streams.value.log

      val project = thisProject.value
      val projectRef = thisProjectRef.value
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
              crossVersion = crossVersionExport(dep.crossVersion),
              extraAttributes = dep.extraAttributes,
              excludes = excludes,
              configurations = dep.configurations,
              platformOpt = dep.crossVersion match {
                case b: CrossVersion.Binary if b.prefix.contains("sjs") || b.suffix.contains("sjs")       => Some(PlatformExport.ScalaJS)
                case b: CrossVersion.Binary if b.prefix.contains("native") || b.suffix.contains("native") => Some(PlatformExport.ScalaNative)
                case f: CrossVersion.Full   if f.prefix.contains("sjs") || f.suffix.contains("sjs")       => Some(PlatformExport.ScalaJS)
                case f: CrossVersion.Full   if f.prefix.contains("native") || f.suffix.contains("native") => Some(PlatformExport.ScalaNative)
                case _ => None
              },
              isChanging = dep.isChanging,
              isTransitive = dep.isTransitive,
              isForce = dep.isForce,
              branchName = dep.branchName
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
          sourceDirs = (projectRef / Compile / sourceDirectories).value.map(_.getAbsolutePath),
          testSourceDirs = (projectRef / Test / sourceDirectories).value.map(_.getAbsolutePath),
          resourceDirs = (projectRef / Compile / resourceDirectories).value.map(_.getAbsolutePath),
          testResourceDirs = (projectRef / Test / resourceDirectories).value.map(_.getAbsolutePath),
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

  @scala.annotation.nowarn("cat=deprecation")
  private def crossVersionExport(cv: sbt.librarymanagement.CrossVersion): CrossVersionExport = cv match {
    case _: CrossVersion.Binary   => CrossVersionExport.Binary
    case _: CrossVersion.Full     => CrossVersionExport.Full
    case _: CrossVersion.Constant => CrossVersionExport.Constant
    case _: CrossVersion.Patch    => CrossVersionExport.Patch
    case CrossVersion.Disabled    => CrossVersionExport.Disabled
    case _                        => CrossVersionExport.Unknown
  }
}
