package com.jaackotorus.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}

import scala.concurrent.Future

object HTTPService {
    import Directives._

    val interface: String = "localhost"
    val port: Int = 8080
    val route: Route =
        get {
            (pathEndOrSingleSlash & redirectToTrailingSlashIfMissing(StatusCodes.TemporaryRedirect)) {
                getFromResource("client/index.html")
            } ~ {
                getFromResourceDirectory("client")
            }
        }

    def apply(interface: String = interface, port: Int = port, route: Route = route): HTTPService = {
        new HTTPService(interface, port, _ => route)
    }
}

class HTTPService(
    interface: String,
    port: Int,
    route: Unit => Route
) extends ServiceBase(interface, port, route)
    with Directives {
    override implicit val system: ActorSystem = ActorSystem("HTTPServiceSystem")

    def start(): Future[Http.ServerBinding] = {
        val bindingFuture: Future[Http.ServerBinding] =
            Http().newServerAt(interface, port).bind(route(()))

        println(s"Server online at http://$interface:$port/")

        bindingFuture
    }
}
