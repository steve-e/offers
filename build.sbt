import sbt.Keys._
lazy val root = (project in file("."))
  .enablePlugins(PlayService, PlayLayoutPlugin, Common)
  .settings(
    name := """offers""",
    version := "2.8.x",
    organization := "com.github.stevee",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      guice,
      "com.typesafe.play" %% "play-slick" % "5.0.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
      "com.h2database" % "h2" % "1.4.199",
      "org.joda" % "joda-convert" % "2.2.1",
      "net.logstash.logback" % "logstash-logback-encoder" % "6.2",
      "io.lemonlabs" %% "scala-uri" % "1.5.1",
      "net.codingwell" %% "scala-guice" % "4.2.6",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      specs2 % Test,
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )