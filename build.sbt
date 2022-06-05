ThisBuild / organization := "com.jaackotorus"

ThisBuild / version := "0.0.0"

ThisBuild / scalaVersion := "3.1.1"

enablePlugins(JavaAppPackaging)

// ThisBuild or Compile?
ThisBuild / mainClass := Some("com.jaackotorus.Main")

lazy val root = (project in file("."))
    .settings(
      name := "driver",
      // scala 3
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "3.2.12" % "test"
      ),
      // scala 2
      libraryDependencies ++= Seq(
        // akka actor typed
        "com.typesafe.akka" %% "akka-actor-typed" % "2.6.8",
        // akka streams
        "com.typesafe.akka" %% "akka-stream" % "2.6.8",
        "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.8",
        // akka http
        "com.typesafe.akka" %% "akka-http-core" % "10.2.9",
        "com.typesafe.akka" %% "akka-http-testkit" % "10.2.9",
        "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.9"
      ).map(_.cross(CrossVersion.for3Use2_13))
    )
