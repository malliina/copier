val copier = project
  .in(file("."))
  .settings(
    version := "0.0.1",
    scalaVersion := "3.7.1",
    libraryDependencies ++= Seq(
      "com.malliina" %% "logback-fs2" % "2.8.3",
      "co.fs2" %% "fs2-io" % "3.11.0",
      "ch.qos.logback" % "logback-classic" % "1.5.18",
      "org.scalameta" %% "munit" % "1.1.1" % Test,
      "org.typelevel" %% "munit-cats-effect" % "2.1.0" % Test
    )
  )

Global / onChangedBuildSource := ReloadOnSourceChanges
