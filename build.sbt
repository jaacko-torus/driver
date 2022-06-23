import com.typesafe.config.ConfigFactory

import java.io.File

lazy val root = (project in file("."))
  .settings(
    name := "driver",
    mainClass := Some(s"$organization.Program"),
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

val conf = ConfigFactory.parseFile(new File("src/main/resources/application.conf"))

ThisBuild / version := conf.getString("driver.build.version")
ThisBuild / scalaVersion := conf.getString("driver.build.scalaVersion")
ThisBuild / organizationName := conf.getString("driver.build.organizationName")
ThisBuild / organization := conf.getString("driver.build.organization")
ThisBuild / idePackagePrefix := Some(conf.getString("driver.build.organization"))

val dockerOrganization = conf.getString("driver.docker.organization")
val akkaVersion = "2.6.8"
val akkaHttpVersion = "10.2.9"

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
  val cmd_args = Seq(
    s"--interface=${conf.getString("driver.docker.interface")}",
    s"--client-source=${conf.getString("driver.docker.client-source")}"
  )

  immutable.Dockerfile.empty
    .from("openjdk:18")
    .workDir("/root")
    // dependencies
    .add(managed_classes.files, "./")
    // main app
    .add(jar, jar.getName)
    // client
    .add(client, "./client")
    .expose(conf.getInt("driver.port.http"), conf.getInt("driver.port.ws"))
    .cmd(cmd_basic ++ cmd_args: _*)
}

docker / imageNames := Seq(
  ImageName(s"$dockerOrganization/${name.value}:latest"),
  ImageName(s"$dockerOrganization/${name.value}:v${version.value}")
)
