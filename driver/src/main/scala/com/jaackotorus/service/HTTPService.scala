package com.jaackotorus.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}

import scala.concurrent.{ExecutionContextExecutor, Future}

object HTTPService extends ServiceBaseTrait[Unit, HTTPService] {
  import Directives._

  override val interface: String = "localhost"
  override val port: Int = 9000
  override val routeGenerator: Unit => Route = _ =>
    get {
      (pathEndOrSingleSlash & redirectToTrailingSlashIfMissing(StatusCodes.TemporaryRedirect)) {
        getFromResource("client/index.html")
      } ~ {
        getFromResourceDirectory("client")
      }
    }

  override def apply(
      interface: String = interface,
      port: Int = port,
      routeGenerator: Unit => Route = routeGenerator
  )(implicit system: ActorSystem, context: ExecutionContextExecutor): HTTPService = {
    new HTTPService(interface, port, routeGenerator)
  }
}

class HTTPService(
    interface: String,
    port: Int,
    route: Unit => Route
)(implicit system: ActorSystem, context: ExecutionContextExecutor)
    extends ServiceBase[Unit](interface, port, route)
    with Directives {

  def start(): Future[Http.ServerBinding] = {
    val bindingFuture: Future[Http.ServerBinding] =
      Http().newServerAt(interface, port).bind(route(()))

    println(s"Server online at http://$interface:$port/")

    bindingFuture
  }
}
