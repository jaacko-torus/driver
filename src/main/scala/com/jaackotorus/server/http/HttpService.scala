package com.jaackotorus.server.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object HttpService {
    val content =
        "<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Commodi corporis dolorum ea excepturi impedit nostrum quasi quisquam soluta voluptate voluptatem. Est, necessitatibus, repudiandae! Aliquam cumque necessitatibus neque repellendus repudiandae. Cupiditate.</p>"

    val serviceHost = "localhost"
    val servicePort = 8080
    val servicePath = "http"

    val route: Route = path(servicePath) {
        get {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, content))
        }
    }

    def main(args: Array[String]): Unit = {
        implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")

        // needed for the future flatMap/onComplete in the end
        implicit val executionContext: ExecutionContextExecutor = system.executionContext

        val bindingFuture = Http().newServerAt(serviceHost, servicePort).bind(route)

        println(s"Server now online. Please navigate to http://$serviceHost:$servicePort/$servicePath")
        println("Press RETURN to stop...")

        StdIn.readLine() // let it run until user presses return

        bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done
    }
}
