val copier = project
  .in(file("."))
  .settings(
    version := "0.0.1",
    scalaVersion := "3.4.0",
    libraryDependencies ++= Seq(
      "com.malliina" %% "logback-fs2" % "2.8.0",
      "co.fs2" %% "fs2-io" % "3.10.2",
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      "org.scalameta" %% "munit" % "1.0.1" % Test,
      "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test
    )
  )

Global / onChangedBuildSource := ReloadOnSourceChanges
