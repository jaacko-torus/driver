package com.jaackotorus.client

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.model.ws.*
import akka.stream.scaladsl.*

import scala.concurrent.Future

object WebsocketClient {
    def main(args: Array[String]): Unit = {
        implicit val system: ActorSystem = ActorSystem()
        import system.dispatcher

        val incoming: Sink[Message, Future[Done]] =
            Sink.foreach[Message] {
                case message: TextMessage.Strict =>
                    println(message.text)
                case _ =>
            }

        val outgoing = Source.single(TextMessage("hello world!"))

        val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest("ws://localhost:8080/greeter"))

        val (upgradeResponse, closed) = outgoing
            .viaMat(webSocketFlow)(Keep.right)
            .toMat(incoming)(Keep.both)
            .run()

        val connected = upgradeResponse.flatMap { upgrade =>
            if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
                Future.successful(Done)
            } else {
                throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
            }
        }

        connected.onComplete(println)
        closed.foreach(_ => println("closed"))
    }
}
