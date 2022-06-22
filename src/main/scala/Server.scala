package com.jaackotorus
import service.{HTTP, WS}

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Server {
  //    val bindingFutures =
  //      List(WebsocketService, HTTPService)
  //        .map { Service =>
  //          implicit val system: ActorSystem = ActorSystem(s"${Service.getClass.getSimpleName.dropRight(1)}System")
  //          implicit val context: ExecutionContextExecutor = system.dispatcher
  //          val service = Service.apply()
  //          (service, service.start())
  //

  def run(config: Program.Config): Unit = {
    val bindingFutures = List(
      {
        implicit val system: ActorSystem = ActorSystem("WebsocketServiceSystem")
        implicit val context: ExecutionContextExecutor = system.dispatcher
        val service = WS(config.interface, 8081)
        (service, service.start())
      }, {
        implicit val system: ActorSystem = ActorSystem("HTTPServiceSystem")
        implicit val context: ExecutionContextExecutor = system.dispatcher
        val service = HTTP(config.interface, 8080, HTTP.`routeGenerator+clientDir`(config.client_source))
        (service, service.start())
      }
    )

    println("Press [RETURN] to stop...")
    StdIn.readLine()

    bindingFutures.foreach { case (service, bindingFuture) =>
      implicit val system: ActorSystem = service.system
      implicit val executionContext: ExecutionContextExecutor = system.dispatcher

      bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())
    }
  }
}
