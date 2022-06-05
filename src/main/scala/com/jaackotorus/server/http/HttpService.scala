package com.jaackotorus.server.http

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
    val content = "<h1>Say hello to akka-http</h1>"

    val route: Route = path("http") {
        get {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, content))
        }
    }

    def main(args: Array[String]): Unit = {
        implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")

        // needed for the future flatMap/onComplete in the end
        implicit val executionContext: ExecutionContextExecutor = system.executionContext

        val host = "localhost"
        val port = 8080

        val bindingFuture = Http().newServerAt(host, port).bind(route)

        println(s"Server now online. Please navigate to https://$host:$port/hello")
        println("Press RETURN to stop...")

        StdIn.readLine() // let it run until user presses return

        bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done
    }
}
