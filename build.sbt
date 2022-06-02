ThisBuild / organization := "com.jaackotorus"

ThisBuild / version := "0.0.0"

ThisBuild / scalaVersion := "3.1.1"

enablePlugins(JavaAppPackaging)

// ThisBuild or Compile?
ThisBuild / mainClass := Some("com.jaackotorus.server.Main")

lazy val root = (project in file("."))
    .settings(
      name := "rider",
      libraryDependencies ++= Seq(
        // akka streams
        "com.typesafe.akka" %% "akka-stream" % "2.6.8",
        "com.typesafe.akka" %% "akka-actor-typed" % "2.6.8",
        // akka http
        "com.typesafe.akka" %% "akka-http" % "10.2.9",
        "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.9"
      ).map(_.cross(CrossVersion.for3Use2_13))
    )
