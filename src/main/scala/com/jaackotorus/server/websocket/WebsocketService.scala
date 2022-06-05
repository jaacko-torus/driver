package com.jaackotorus.server.websocket

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

class WebsocketService extends Directives {
    implicit val actorSystem: ActorSystem = ActorSystem()

    val websocketRoute: Route = path("websocket") {
        get {
            handleWebSocketMessages(greeter)
        }
    }

    def greeter: Flow[Message, Message, Any] =
        Flow[Message].collect { case TextMessage.Strict(t) =>
            TextMessage(t)
//            case BinaryMessage.Strict(b) =>
//                // ignore binary messages but drain content to avoid the stream being clogged
//                // b.runWith(Sink.ignore)
//                Nil
        }
}
