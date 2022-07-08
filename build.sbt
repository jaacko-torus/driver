import com.typesafe.config.ConfigFactory

import java.io.File

val conf = ConfigFactory.parseFile(new File("src/main/resources/application.conf"))
val akkaVersion = "2.6.8"
val akkaHttpVersion = "10.2.9"

ThisBuild / organization := conf.getString("driver.build.organization")
ThisBuild / version := conf.getString("driver.build.version")
ThisBuild / scalaVersion := conf.getString("driver.build.scalaVersion")
ThisBuild / organizationName := conf.getString("driver.build.organizationName")
ThisBuild / licenses := Seq("MIT" -> url("https://mit-license.org"))

ThisBuild / mainClass := Some(s"$organization.Program")

Global / excludeLintKeys += idePackagePrefix

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "driver",
    idePackagePrefix := Some(conf.getString("driver.build.organization")),
    libraryDependencies := Seq(
      // config parsing
      "com.typesafe" % "config" % "1.4.2",

      // nscala time
      "com.github.nscala-time" %% "nscala-time" % "2.30.0",

      // argument processing
      "org.rogach" %% "scallop" % "4.1.0",
      "com.github.scopt" %% "scopt" % "4.0.1",

      // akka typed
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,

      // akka stream
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,

      // akka http
      "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
    )
  )
