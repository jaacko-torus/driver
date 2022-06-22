package com.jaackotorus
package service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.reject
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ServiceTrait[T <: Any, U <: Service[T]] {
  val interface = "0.0.0.0"
  val port = 0
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

abstract class Service[T <: Any](
    val interface: String,
    val port: Int,
    val route: T => Route
)(implicit val system: ActorSystem, val context: ExecutionContextExecutor) {
  def start(): Future[Http.ServerBinding]
}
