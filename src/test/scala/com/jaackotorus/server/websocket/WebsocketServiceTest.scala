package com.jaackotorus.server.websocket

//import akka.http.scaladsl.model.ws.TextMessage
//import org.scalatest.funsuite.AnyFunSuite
//import org.scalatest.matchers.should.Matchers
//import akka.http.scaladsl.testkit.WSTestRequestBuilding.WS
//import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
//
//class WebsocketServiceTest extends AnyFunSuite with Matchers with ScalatestRouteTest {
//    test("empty") {
//        1 shouldBe 1
//    }
//
//    test("connect to websocket server") {
//        val wsService = WebsocketService()
//        val wsClient = WSProbe()
//
//        WS("/websocket", wsClient.flow) -> wsService.websocketRoute -> check {
//            isWebSocketUpgrade shouldEqual true
//        }
//    }
//
//    test("respond with correct message") {
//        val wsService = WebsocketService()
//        val wsClient = WSProbe()
//
//        WS("/websocket", wsClient.flow) -> wsService.websocketRoute -> check {
//            wsClient.sendMessage("hello from client")
//            wsClient.expectMessage("hello from client")
//        }
//    }
//}
