import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val foo = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .settings(
    scalaVersion := "2.13.16",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "4.1.0"
  )
  .jvmSettings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0"
  )
  .nativeSettings(
    libraryDependencies += "com.lihaoyi" %%% "fansi" % "0.5.0"
  )
