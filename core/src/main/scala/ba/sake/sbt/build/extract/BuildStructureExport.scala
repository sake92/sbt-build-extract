package ba.sake.sbt.build.extract

import upickle.default.{ReadWriter, macroRW}

case class BuildStructureExport(
    projects: Seq[ProjectExport]
)

object BuildStructureExport {
  implicit val rw: ReadWriter[BuildStructureExport] = macroRW
}

case class ProjectExport(
    scope: String,
    id: String,
    base: String,
    name: String,
    scalaVersion: String,
    organization: String,
    version: String,
    // publishTo: String, // TODO
    homepage: Option[String],
    externalDependencies: Seq[DependencyExport],
    interProjectDependencies: Seq[InterProjectDependencyExport],
    javacOptions: Seq[String],
    scalacOptions: Seq[String]
)

object ProjectExport {
  implicit val rw: ReadWriter[ProjectExport] = macroRW
}

case class DependencyExport(
    organization: String,
    name: String,
    revision: String,
    crossVersion: String // TODO structured crossVersion
)

object DependencyExport {
  implicit val rw: ReadWriter[DependencyExport] = macroRW
}

case class InterProjectDependencyExport(
    project: String,
    configuration: String
)

object InterProjectDependencyExport {
  implicit val rw: ReadWriter[InterProjectDependencyExport] = macroRW
}
