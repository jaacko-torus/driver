package com.jaackotorus
import service.{HTTP, Service, WS}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Flow

import scala.concurrent.{ExecutionContextExecutor, Future}
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
    type y = List[
      (
          Service[_ >: (String => Flow[Message, Message, Any]) with Unit],
          Future[ServerBinding]
      )
    ]
    val bindingFutures: y = List(
      WS(
        config.interface,
        config.port_ws,
        WS.routeGenerator
      ),
      HTTP(
        config.interface,
        config.port_http,
        HTTP.`routeGenerator+clientDir`(config.client_source)
      )
    ).map(_.start)

    bindingFutures.foreach { case (service, _) =>
      println(s"${service.getClass.getSimpleName} service running on: ${config.interface}:${service.port}")
    }
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
