
val scala212 = "2.12.18"
val scala213 = "2.13.16"
val scala3 = "3.3.5"
ThisBuild / scalaVersion := scala212
ThisBuild / organization := "ba.sake"
ThisBuild / homepage := Some(url("https://sake92.github.io/sbt-build-extract"))
ThisBuild / licenses := List("MIT" -> url("https://spdx.org/licenses/MIT.html"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/sake92/sbt-build-extract"),
    "scm:git:git@github.com:sake92/sbt-build-extract.git"
  )
)
ThisBuild / developers := List(
   Developer(
     "sake92",
     "Sakib Hadžiavdić",
     "sakib@sake.ba",
     url("https://sake.ba")
   )
)

lazy val core = (project in file("core"))
  .settings(
    name := "sbt-build-extract-core",
    description := "Core models for sbt-build-extract",
    crossScalaVersions := List(scala212, scala213, scala3),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "upickle" % "4.1.0"
    )
  )

lazy val plugin = (project in file("plugin"))
  .settings(
    name := "sbt-build-extract",
    description := "Sbt plugin for extracting build information",
    sbtPlugin := true
  )
  .dependsOn(core)
