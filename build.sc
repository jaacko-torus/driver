import mill._
import mill.scalalib._

object driver extends ScalaModule {
    override def mainClass = Option("com.jaackotorus.Server")

    override def scalaVersion = "2.13.8"

    val akkaVersion = "2.6.8"
    val akkaHttpVersion = "10.2.9"
    override def ivyDeps = Agg(
      // nscala time
      ivy"com.github.nscala-time::nscala-time:2.30.0",
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
