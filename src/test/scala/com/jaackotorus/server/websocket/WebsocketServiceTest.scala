package com.jaackotorus.server.websocket

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.testkit.WSTestRequestBuilding.WS
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WebsocketServiceTest extends AnyFunSuite with Matchers with ScalatestRouteTest {
    test("empty") {
        1 shouldBe 1
    }

    test("should connect to websocket server") {
        val wsService = WebsocketService()

        val wsClient = WSProbe()

        WS("/greeter", wsClient.flow) -> wsService.websocketRoute -> check {
            isWebSocketUpgrade shouldEqual true
        }
    }
}
