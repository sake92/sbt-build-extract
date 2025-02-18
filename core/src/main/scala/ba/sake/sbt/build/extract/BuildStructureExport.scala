package ba.sake.sbt.build.extract

import upickle.default.{ReadWriter, macroRW}

case class ProjectExport(
    id: String,
    base: String, // base directory
    name: String,
    javacOptions: Seq[String],
    scalaVersion: String,
    crossScalaVersions: Seq[String],
    scalacOptions: Seq[String],
    interProjectDependencies: Seq[InterProjectDependencyExport],
    externalDependencies: Seq[DependencyExport],
    repositories: Seq[String], // custom maven repos
    sourceDirs: Seq[String],
    testSourceDirs: Seq[String],
    resourceDirs: Seq[String],
    testResourceDirs: Seq[String],
    plugins: Seq[String],
    // publish stuff
    organization: String, // groupId
    artifactName: String,
    artifactType: String, // jar, war ..
    artifactClassifier: Option[String], // sources, javadoc ..
    version: String,
    description: String,
    homepage: Option[String],
    developers: Seq[DeveloperExport],
    licenses: Seq[LicenseExport],
    scmInfo: Option[ScmInfoExport]
)

object ProjectExport {
  implicit val rw: ReadWriter[ProjectExport] = macroRW
}

// TODO structured crossVersion?
case class DependencyExport(
    organization: String, // groupId
    name: String, // artifactName
    revision: String, // version
    extraAttributes: Map[String, String], // type, classifier ..
    configurations: Option[String], // provided, test ..
    excludes: Seq[DependencyExcludeExport],
    crossVersion: String
)

object DependencyExport {
  implicit val rw: ReadWriter[DependencyExport] = macroRW
}

case class DependencyExcludeExport(
    organization: String, // groupId
    name: String // artifactName
)

object DependencyExcludeExport {
  implicit val rw: ReadWriter[DependencyExcludeExport] = macroRW
}

case class InterProjectDependencyExport(
    project: String,
    configuration: String
)

object InterProjectDependencyExport {
  implicit val rw: ReadWriter[InterProjectDependencyExport] = macroRW
}

case class DeveloperExport(id: String, name: String, email: String, url: String)

object DeveloperExport {
  implicit val rw: ReadWriter[DeveloperExport] = macroRW
}

case class LicenseExport(name: String, url: String)

object LicenseExport {
  implicit val rw: ReadWriter[LicenseExport] = macroRW
}

case class ScmInfoExport(browseUrl: String, connection: String, devConnection: Option[String])

object ScmInfoExport {
  implicit val rw: ReadWriter[ScmInfoExport] = macroRW
}
