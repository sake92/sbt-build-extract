val scala212 = "2.12.18"
val scala213 = "2.13.16"
val scala3 = "3.8.4"
ThisBuild / scalaVersion := scala3
ThisBuild / organization := "ba.sake"
ThisBuild / homepage := Some(uri("https://sake92.github.io/sbt-build-extract"))
ThisBuild / licenses := List(License("MIT", uri("https://spdx.org/licenses/MIT.html")))
ThisBuild / scmInfo := Some(
  ScmInfo(
    uri("https://github.com/sake92/sbt-build-extract"),
    "scm:git:git@github.com:sake92/sbt-build-extract.git"
  )
)
ThisBuild / developers := List(
  Developer(
    "sake92",
    "Sakib Hadžiavdić",
    "sakib@sake.ba",
    uri("https://sake.ba")
  )
)

lazy val core = (project in file("core"))
  .settings(
    name := "sbt-build-extract-core",
    description := "Core models for sbt-build-extract",
    crossScalaVersions := List(scala212, scala213, scala3),
    externalResolvers := {
      val configuredResolvers = appResolvers.value.toSeq.flatten ++ resolvers.value
      Resolver.defaultLocal +: configuredResolvers
    },
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "upickle" % "4.1.0"
    )
  )

lazy val plugin = (project in file("plugin"))
  .enablePlugins(SbtPlugin, ScriptedPlugin)
  .settings(
    name := "sbt-build-extract",
    description := "Sbt plugin for extracting build information",
    externalResolvers := {
      val configuredResolvers = appResolvers.value.toSeq.flatten ++ resolvers.value
      Resolver.defaultLocal +: configuredResolvers
    },
    pluginCrossBuild / sbtVersion := "2.0.0",
    scriptedSbt := "2.0.12",
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )
  .dependsOn(core)
