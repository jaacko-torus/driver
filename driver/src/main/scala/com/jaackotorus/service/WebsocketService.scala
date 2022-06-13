package com.jaackotorus.service

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props, Status}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream._
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import com.jaackotorus.actor.ChatroomActor

import scala.concurrent.{ExecutionContextExecutor, Future}

object WebsocketService {
    import Directives.{get, handleWebSocketMessages, parameter, path}

    val interface: String = "localhost"
    val port: Int = 8081
    val routeGenerator: (String => Flow[Message, Message, Any]) => Route = service =>
        path("greeter") {
            (get & parameter("username")) { username =>
                handleWebSocketMessages(service(username))
            }
        }

    def apply(
        interface: String = interface,
        port: Int = port,
        routeGenerator: (String => Flow[Message, Message, Any]) => Route = routeGenerator
    ): WebsocketService = {
        new WebsocketService(interface, port, routeGenerator)
    }
}

class WebsocketService(
    interface: String,
    port: Int,
    route: (String => Flow[Message, Message, Any]) => Route
) extends ServiceBase(interface, port, route)
    with Directives {
    import ChatroomActor.{Event, User}

    implicit val system: ActorSystem = ActorSystem("WebsocketServiceSystem")
    implicit val context: ExecutionContextExecutor = system.dispatcher

    val chatroomActor: ActorRef = system.actorOf(Props(new ChatroomActor()))

    val userActorSource: Source[Event, ActorRef] = {
        val completionMatcher: PartialFunction[Any, CompletionStrategy] = {
            case akka.actor.Status.Success(s: CompletionStrategy) => s
            case akka.actor.Status.Success(_)                     => CompletionStrategy.draining
            case akka.actor.Status.Success                        => CompletionStrategy.draining
        }

        val failureMatcher: PartialFunction[Any, Throwable] = { case akka.actor.Status.Failure(cause) => cause }

        Source.actorRef[Event](completionMatcher, failureMatcher, 5, OverflowStrategy.fail)
    }

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
            Http().newServerAt(interface, port).bind(route(service _))

        println(s"Server online at http://$interface:$port/")

        bindingFuture
    }
}
