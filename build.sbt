val copier = project
  .in(file("."))
  .settings(
    version := "0.0.1",
    scalaVersion := "3.8.1",
    libraryDependencies ++= Seq(
      "com.malliina" %% "logback-fs2" % "6.11.1",
      "co.fs2" %% "fs2-io" % "3.12.2",
      "ch.qos.logback" % "logback-classic" % "1.5.31",
      "org.scalameta" %% "munit" % "1.2.2" % Test,
      "org.typelevel" %% "munit-cats-effect" % "2.1.0" % Test
    )
  )

Global / onChangedBuildSource := ReloadOnSourceChanges
