package com.jaackotorus
package service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ServiceTrait[T <: Any, U <: Service[T]] {
//  val interface: String
//  val port: Int
//  val routeGenerator: T => Route
  def apply(
      interface: String, // = interface,
      port: Int, // = port,
      routeGenerator: T => Route // = routeGenerator
  ): U
}

abstract class Service[T <: Any](
    val interface: String,
    val port: Int,
    val route: T => Route
) {
  implicit val system: ActorSystem
  implicit val context: ExecutionContextExecutor
  def start: (Service[T], Future[Http.ServerBinding])
}
