package com.jaackotorus.server

import akka.actor.ActorSystem

import com.jaackotorus.server.service.WebsocketService

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Server {
    def main(args: Array[String]): Unit = {
        val websocketService = WebsocketService()
        implicit val system: ActorSystem = websocketService.actorSystem
        implicit val executionContext: ExecutionContextExecutor = system.dispatcher

        val bindingFuture = websocketService.start()
        StdIn.readLine() // let it run until user presses return
        bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done
    }
}
