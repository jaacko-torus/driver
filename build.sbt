import com.typesafe.config.ConfigFactory

val docker_organization = "jaackotorus"
ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / organizationName := "jaacko-torus"
ThisBuild / organization := s"com.jaackotorus"
ThisBuild / idePackagePrefix := Some(organization.value)

Global / libraryDependencies += "com.typesafe" % "config" % "1.4.2"

val conf = ConfigFactory.parseFile(new File("src/main/resources/application.conf"))

val akkaVersion = "2.6.8"
val akkaHttpVersion = "10.2.9"
lazy val root = (project in file("."))
  .settings(
    name := "driver",
    mainClass := Some(s"$organization.Program"),
    libraryDependencies := Seq(
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

import sbtdocker.immutable

enablePlugins(DockerPlugin)

docker / dockerfile := {
  // TODO: Research this part below
  val jar: File = (Compile / packageBin / sbt.Keys.`package`).value
  val managed_classes = (Compile / managedClasspath).value
  val main_class = (Compile / packageBin / mainClass).value.getOrElse(sys.error("Expected exactly one main class"))
  val resources = (Compile / resourceDirectory).value

  // Make a colon separated classpath with the JAR file
  val all_classes = managed_classes.files :+ jar

  // `resources/client` will always contain the client directory
  val client = new File(resources, "client")

  // TODO: give error in case file doesn't exist

  val cmd_basic = Seq("java", "-cp", all_classes.map(_.getName).mkString(":"), main_class)
  val cmd_args = Seq("--interface=0.0.0.0", "--client-source=client")

  immutable.Dockerfile.empty
    .from("openjdk:18")
    .workDir("/root")
    // dependencies
    .add(managed_classes.files, "./")
    // main app
    .add(jar, jar.getName)
    // client
    .add(client, "./client")
    .expose(conf.getInt("ports.http"), conf.getInt("ports.ws"))
    .cmd(cmd_basic ++ cmd_args: _*)
}

docker / imageNames := Seq(
  ImageName(s"$docker_organization/${name.value}:latest"),
  ImageName(s"$docker_organization/${name.value}:v${version.value}")
)
