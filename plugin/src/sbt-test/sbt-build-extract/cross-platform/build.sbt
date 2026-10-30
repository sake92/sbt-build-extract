lazy val foo = (projectMatrix in file("."))
  .settings(
    scalaVersion := "2.13.16",
    libraryDependencies += "com.lihaoyi" %% "upickle" % "4.1.0"
  )
  .jvmPlatform(
    scalaVersions = Seq("2.13.16"),
    settings = Seq(
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )
  .jsPlatform(
    scalaVersions = Seq("2.13.16"),
    settings = Seq(
      libraryDependencies += "org.scala-js" %% "scalajs-dom" % "2.8.0"
    )
  )
  .nativePlatform(
    scalaVersions = Seq("2.13.16"),
    settings = Seq(
      libraryDependencies += "com.lihaoyi" %% "fansi" % "0.5.0"
    )
  )
