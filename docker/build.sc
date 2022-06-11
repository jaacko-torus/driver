import mill._
import mill.api._
import mill.scalalib._

object driver extends ScalaModule {
    override def mainClass: T[Option[String]] = Option("com.jaackotorus.server.Server")

    override def scalaVersion: T[String] = "2.13.8"
    
    override def ivyDeps: T[Loose.Agg[Dep]] = Agg(
      // typed
        ivy"com.typesafe.akka::akka-actor-typed:2.6.8",
      // stream
        ivy"com.typesafe.akka::akka-stream:2.6.8",
        ivy"com.typesafe.akka::akka-stream-testkit:2.6.8",
      // http
        ivy"com.typesafe.akka::akka-http-core:10.2.9",
        ivy"com.typesafe.akka::akka-http-testkit:10.2.9",
        ivy"com.typesafe.akka::akka-http-spray-json:10.2.9"
    )
}