package com.jaackotorus

import akka.actor.ActorSystem
import com.jaackotorus.service.{HTTPService, WebsocketService}

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
        val service = WebsocketService(config.interface)
        (service, service.start())
      }, {
        implicit val system: ActorSystem = ActorSystem("HTTPServiceSystem")
        implicit val context: ExecutionContextExecutor = system.dispatcher
        val service = HTTPService(config.interface)
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
