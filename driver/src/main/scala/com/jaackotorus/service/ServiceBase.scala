package com.jaackotorus.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.concurrent.Future

abstract class ServiceBase[T <: Any](
    val interface: String,
    val port: Int,
    val route: T => Route
) {
    implicit val system: ActorSystem
    def start(): Future[Http.ServerBinding]
}
