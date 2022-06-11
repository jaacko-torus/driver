package com.jaackotorus.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.jaackotorus.server.service.WebsocketService
import com.jaackotorus.server.service.WebsocketService.system

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Server {
    def main(args: Array[String]): Unit = {
        val websocketService = WebsocketService.start()
        import WebsocketService.{system, executionContext}

        StdIn.readLine() // let it run until user presses return
        websocketService
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done
    }
}
