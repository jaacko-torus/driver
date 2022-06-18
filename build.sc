import mill._
import mill.define.Target
import mill.scalalib._

object driver extends ScalaModule {
  val akkaVersion = "2.6.8"
  val akkaHttpVersion = "10.2.9"

  override def mainClass = Option("com.jaackotorus.Program")

  override def scalaVersion = "2.13.8"

  override def scalacOptions: Target[Seq[String]] = Seq("-deprecation")

  override def ivyDeps = Agg(
    // nscala time
    ivy"com.github.nscala-time::nscala-time:2.30.0",
    // argument processing
    ivy"org.rogach::scallop:4.1.0",
    ivy"com.github.scopt::scopt:4.0.1",
    // akka typed
    ivy"com.typesafe.akka::akka-actor-typed:$akkaVersion",
    // akka stream
    ivy"com.typesafe.akka::akka-stream:$akkaVersion",
    ivy"com.typesafe.akka::akka-stream-testkit:$akkaVersion",
    // akka http
    ivy"com.typesafe.akka::akka-http-core:$akkaHttpVersion",
    ivy"com.typesafe.akka::akka-http-testkit:$akkaHttpVersion",
    ivy"com.typesafe.akka::akka-http-spray-json:$akkaHttpVersion"
  )
}
