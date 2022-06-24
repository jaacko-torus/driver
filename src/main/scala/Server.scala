package com.jaackotorus
import service.{HTTP, WS}

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.language.existentials

object Server {
  //    val bindingFutures =
  //      List(WebsocketService, HTTPService)
  //        .map { Service =>
  //          implicit val system: ActorSystem = ActorSystem(s"${Service.getClass.getSimpleName.dropRight(1)}System")
  //          implicit val context: ExecutionContextExecutor = system.dispatcher
  //          val service = Service.apply()
  //          (service, service.start())
  //

  // TODO: merge Config & Conf
  def run(config: Program.Config): Unit = {
    val bindingFutures = List(
      WS(
        config.interface,
        config.port_ws,
        WS.routeGenerator
      ).start,
      HTTP(
        config.interface,
        config.port_http,
        HTTP.`routeGenerator+clientDir`(config.client_source)
      ).start
    )

    bindingFutures.foreach { case (service, _) =>
      println(s"${service.getClass.getSimpleName} service running on: ${config.interface}:${service.port}")
    }

    println("Press [RETURN] to stop...")
    StdIn.readLine()

    if (config.interactive) {
      bindingFutures.foreach { case (service, bindingFuture) =>
        implicit val system: ActorSystem = service.system
        implicit val executionContext: ExecutionContextExecutor = system.dispatcher

        bindingFuture
          .flatMap(_.unbind())
          .onComplete(_ => system.terminate())
      }
    }
  }
}
