package com.jaackotorus.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.reject
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ServiceBaseTrait[T <: Any, U <: ServiceBase[T]] {
  val interface = "localhost"
  val port = 8080
  val routeGenerator: T => Route = _ => reject
  def apply(
      interface: String = interface,
      port: Int = port,
      routeGenerator: T => Route = routeGenerator
  )(implicit
      system: ActorSystem,
      context: ExecutionContextExecutor
  ): U
}

abstract class ServiceBase[T <: Any](
    val interface: String,
    val port: Int,
    val route: T => Route
)(implicit val system: ActorSystem, val context: ExecutionContextExecutor) {
  def start(): Future[Http.ServerBinding]
}
