package com.jaackotorus
package service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}

import scala.annotation.unused
import scala.concurrent.{ExecutionContextExecutor, Future}

object HTTP extends ServiceTrait[Unit, HTTP] {
  import Directives._

  def `routeGenerator+clientDir`(clientDir: String)(@unused value: Unit): Route =
    get {
      (pathEndOrSingleSlash & redirectToTrailingSlashIfMissing(StatusCodes.TemporaryRedirect)) {
        println(clientDir)
        getFromFile(s"$clientDir/index.html")
      } ~ {
        getFromDirectory(clientDir)
      }
    }

  val clientDir = ""

  // def clientDir: Unit => String// = "src/main/resources/client"

  override def apply(
      interface: String = interface,
      port: Int = port,
      routeGenerator: Unit => Route = routeGenerator
  )(implicit system: ActorSystem, context: ExecutionContextExecutor): HTTP = {
    new HTTP(interface, port, routeGenerator)
  }
}

class HTTP(
    interface: String,
    port: Int,
    route: Unit => Route
)(implicit system: ActorSystem, context: ExecutionContextExecutor)
    extends Service[Unit](interface, port, route)
    with Directives {

  def start(): Future[Http.ServerBinding] = {
    val bindingFuture: Future[Http.ServerBinding] =
      Http().newServerAt(interface, port).bind(route(()))

    println(s"HTTP server online at http://$interface:$port/")

    bindingFuture
  }
}
