package com.jaackotorus.server

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.AttributeKeys.webSocketUpgrade
import akka.http.scaladsl.model.HttpMethods.*
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, WebSocketRequest}
import akka.http.scaladsl.model.{AttributeKeys, HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.settings.{ClientConnectionSettings, ServerSettings}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, UniformFanInShape}
import akka.util.ByteString
import spray.json.DefaultJsonProtocol.jsonFormat1

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.Future
import scala.io.StdIn

object Server {
    def main(args: Array[String]): Unit = {
        Server()
    }
}

class Server extends Directives {
    implicit val system: ActorSystem = ActorSystem()

    val chatRoomActor: ActorRef = system.actorOf(Props(ChatRoomActor()))

    val websocketService: Flow[Message, TextMessage, Any] =
        Flow.fromGraph(GraphDSL.create() { implicit builder =>
            import GraphDSL.Implicits.*

            val initial = builder.materializedValue.map(_ => TextMessage.Strict("initial message"))
            val messagePassing = builder.add(Flow[Message].collect {
                case tm: TextMessage => TextMessage(tm.textStream)
                case bm: BinaryMessage =>
                    bm.dataStream.runWith(Sink.ignore)
                    TextMessage("This server does not understand binary messages.")
            })
            val merger: UniformFanInShape[Message, Message] = builder.add(Merge[Message](2))
//            val messageToChatRoomEvent = builder.add(Flow[Message].map {
//                case TextMessage.Strict(tm) =>
//                    ChatRoomActor.Event.UserSentMessage("example-username", tm)
//                case _ =>
//            })
//            val ChatRoomEventToMessage = builder.add(Flow[ChatRoomActor.Event].map {
//                case ChatRoomActor.Event.UsersChanged(users) => {
//                    import spray.json.*
//                    import DefaultJsonProtocol.*
//                    implicit val userFormat: RootJsonFormat[ChatRoomActor.User] =
//                        jsonFormat1(ChatRoomActor.User.apply)
//                    TextMessage(users.toJson.toString)
//                }
//            })
//            val chatRoomActorSink =
//                Sink.actorRef[ChatRoomActor.Event](chatRoomActor, ChatRoomActor.Event.UserLeft("example"), _ => ())

            initial ~> merger.in(0)
            merger ~> messagePassing

            FlowShape(merger.in(1), messagePassing.out)
        })

    val route: Route =
        path("greeter") {
            get {
                handleWebSocketMessages(websocketService)
            }
        }

    val bindingFuture: Future[Http.ServerBinding] =
        Http().newServerAt("localhost", 8080).bind(route)

    println(s"Server online at http://localhost:8080/greeter")
    println("Press RETURN to stop...")

    StdIn.readLine()

    import system.dispatcher
    bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())
}
