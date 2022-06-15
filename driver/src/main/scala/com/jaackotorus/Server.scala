package com.jaackotorus

import akka.actor.ActorSystem
import com.jaackotorus.service.{HTTPService, WebsocketService}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Server {
    def main(args: Array[String]): Unit = {

        val bindingFutures = List(
          {
              implicit val system: ActorSystem = ActorSystem("WebsocketServiceSystem")
              implicit val context: ExecutionContextExecutor = system.dispatcher
              val service = WebsocketService()
              (service, service.start())
          }, {
              implicit val system: ActorSystem = ActorSystem("HTTPServiceSystem")
              implicit val context: ExecutionContextExecutor = system.dispatcher
              val service = HTTPService()
              (service, service.start())
          }
        )

        println("Press RETURN to stop...")

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
