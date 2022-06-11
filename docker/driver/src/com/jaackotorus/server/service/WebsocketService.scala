package com.jaackotorus.server.service

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props, Status}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{CompletionStrategy, FlowShape, OverflowStrategy, Shape, UniformFanInShape}

import com.jaackotorus.server.actor.ChatroomActor

import scala.concurrent.{ExecutionContextExecutor, Future}

object WebsocketService {
    import Directives.{path, get, parameter, handleWebSocketMessages}

    def apply(
        interface: String = "localhost",
        port: Int = 8081,
        routeGenerator: (String => Flow[Message, Message, Any]) => Route = service =>
            path("greeter") {
                (get & parameter("username")) { username =>
                    handleWebSocketMessages(service(username))
                }
            }
    ): WebsocketService = {
        new WebsocketService(interface, port, routeGenerator)
    }
}

class WebsocketService(
    interface: String,
    port: Int,
    route: (String => Flow[Message, Message, Any]) => Route
) extends Service(interface, port, route)
    with Directives {
    import ChatroomActor.{Event, User}

    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

    val chatroomActor: ActorRef = actorSystem.actorOf(Props(new ChatroomActor()))

    val userActorSource: Source[Event, ActorRef] =
        Source.actorRef[Event](
          {
              case akka.actor.Status.Success(s: CompletionStrategy) => s
              case akka.actor.Status.Success(_)                     => CompletionStrategy.draining
              case akka.actor.Status.Success                        => CompletionStrategy.draining
          },
          { case akka.actor.Status.Failure(cause) => cause },
          5,
          OverflowStrategy.fail
        )

    type Found = Source[Event, ActorRef]#Shape => FlowShape[Message, TextMessage]
    type Required = Shape => FlowShape[Message, TextMessage]

    def service(username: String): Flow[Message, TextMessage, ActorRef] =
        Flow.fromGraph(GraphDSL.create(userActorSource) {
            implicit builder: GraphDSL.Builder[ActorRef] => (userActor: Source[Event, ActorRef]#Shape) =>
                import GraphDSL.Implicits._

                val initial: PortOps[Event] =
                    builder.materializedValue.map(userActorRef => Event.UserJoined(User(username), userActorRef))

                val merger: UniformFanInShape[Event, Event] = builder.add(Merge[Event](2))

                val messageToEvent: FlowShape[Message, Event] =
                    builder.add(Flow[Message].map {
                        case TextMessage.Strict(message) =>
                            Event.UserSentMessage(username, message)
                        case _ => Event.None()
                    })

                val eventToMessage: FlowShape[Event, TextMessage] =
                    builder.add(Flow[Event].map {
                        case Event.UsersChanged(players) =>
                            import spray.json._
                            import DefaultJsonProtocol._
                            implicit val userFormat: RootJsonFormat[User] = jsonFormat1(User.apply)
                            TextMessage(players.toJson.toString)
                        case Event.UserSentMessage(_, message) =>
                            TextMessage(message)
                        case _ =>
                            TextMessage("N/A")
                    })

                val chatroomActorSink: Sink[Event, NotUsed] =
                    Sink.actorRef[Event](chatroomActor, Event.UserLeft(username), Status.Failure)

                initial ~> merger
                messageToEvent ~> merger
                merger ~> chatroomActorSink

                userActor ~> eventToMessage

                FlowShape(messageToEvent.in, eventToMessage.out)
        })

    def start(): Future[Http.ServerBinding] = {
        val bindingFuture: Future[Http.ServerBinding] =
            Http().newServerAt(interface, port).bind(route(service))

        println(s"Server online at http://$interface:$port/")

        bindingFuture
    }
}
