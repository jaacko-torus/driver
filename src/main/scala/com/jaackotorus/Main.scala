package com.jaackotorus

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

object Main {
    val content: String =
        """
          |<html>
          |  <head><title>Title</title></head>
          |  <body><p>Paragraph 1.</p></body>
		  |</html>
          |""".stripMargin

    val route: Route = get {
        complete(
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            content
          )
        )
    }

    def main(args: Array[String]): Unit = {
        implicit val system: ActorSystem = ActorSystem("MyServer")

        val host = "0.0.0.0"
        val port = sys.env.getOrElse("PORT", "8080").toInt

        Http().bindAndHandle(route, host, port)
    }
}
