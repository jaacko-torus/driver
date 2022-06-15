package com.jaackotorus.service

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props, Status}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream._
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import com.github.nscala_time.time.Imports._

import com.jaackotorus.actor.UserActor

import scala.concurrent.{ExecutionContextExecutor, Future}

object WebsocketService extends ServiceBaseTrait[String => Flow[Message, Message, Any], WebsocketService] {
    import Directives.{get, handleWebSocketMessages, parameter, path}

    override val interface: String = "localhost"
    override val port: Int = 9001
    override val routeGenerator: (String => Flow[Message, Message, Any]) => Route = service =>
        path("greeter") {
            (get & parameter("username")) { username =>
                handleWebSocketMessages(service(username))
            }
        }

    override def apply(
        interface: String = interface,
        port: Int = port,
        routeGenerator: (String => Flow[Message, Message, Any]) => Route = routeGenerator
    )(implicit
        system: ActorSystem,
        context: ExecutionContextExecutor
    ): WebsocketService = {
        new WebsocketService(interface, port, routeGenerator)
    }
}

class WebsocketService(
    interface: String,
    port: Int,
    route: (String => Flow[Message, Message, Any]) => Route
)(implicit system: ActorSystem, context: ExecutionContextExecutor)
    extends ServiceBase[String => Flow[Message, Message, Any]](interface, port, route)
    with Directives {
    import UserActor.{Event, User}

    val chatroomActor: ActorRef = system.actorOf(Props(new UserActor()))

    val userActorSource: Source[Event, ActorRef] = {
        val completionMatcher: PartialFunction[Any, CompletionStrategy] = {
            case akka.actor.Status.Success(s: CompletionStrategy) => s
            case akka.actor.Status.Success(_)                     => CompletionStrategy.draining
            case akka.actor.Status.Success                        => CompletionStrategy.draining
        }

        val failureMatcher: PartialFunction[Any, Throwable] = { case akka.actor.Status.Failure(cause) => cause }

        Source.actorRef[Event](completionMatcher, failureMatcher, 5, OverflowStrategy.fail)
    }

    override def start(): Future[Http.ServerBinding] = {
        val bindingFuture: Future[Http.ServerBinding] =
            Http().newServerAt(interface, port).bind(route(service(_)))

        println(s"Server online at http://$interface:$port/")

        bindingFuture
    }

    def service(username: String): Flow[Message, TextMessage, ActorRef] =
        Flow.fromGraph(GraphDSL.create(userActorSource) {
            implicit builder: GraphDSL.Builder[ActorRef] => (userActor: Source[Event, ActorRef]#Shape) =>
                import GraphDSL.Implicits._

                implicit val datetime: DateTime = DateTime.now()

                val materialization: PortOps[Event] =
                    builder.materializedValue.map(userActorRef => Event.UserJoined(username, userActorRef))

                val merger: UniformFanInShape[Event, Event] = builder.add(Merge[Event](2))

                val messageToEvent: FlowShape[Message, Event] =
                    builder.add(Flow[Message].map {
                        case TextMessage.Strict(message) if message.trim != "" =>
                            Event.UserSentMessage(username, message)
                        case _ => Event.None()
                    })

                import spray.json._
                import DefaultJsonProtocol._

                val eventToMessage: FlowShape[Event, TextMessage] =
                    builder.add(Flow[Event].map {
                        case event: Event.UserSentMessage =>
                            TextMessage(
                              JsObject(
                                "type" -> JsString("message"),
                                "data" -> JsObject(
                                  "username" -> event.username.toJson,
                                  "message" -> event.message.toJson,
                                  "timestamp" -> event.timestamp.toJson
                                )
                              ).toString
                            )
                        case event: Event.UserJoined =>
                            TextMessage(
                              JsObject(
                                "type" -> JsString("user_joined"),
                                "data" -> JsObject(
                                  "username" -> event.username.toJson,
                                  "timestamp" -> event.timestamp.toJson
                                )
                              ).toString
                            )
                        case event: Event.UserLeft =>
                            TextMessage(
                              JsObject(
                                "type" -> JsString("user_left"),
                                "data" -> JsObject(
                                  "username" -> event.username.toJson,
                                  "timestamp" -> event.timestamp.toJson
                                )
                              ).toString
                            )
                    })

                val chatroomActorSink: Sink[Event, NotUsed] =
                    Sink.actorRef[Event](chatroomActor, Event.UserLeft(username), Status.Failure)

                materialization ~> merger
                messageToEvent ~> merger
                merger ~> chatroomActorSink

                userActor ~> eventToMessage

                FlowShape(messageToEvent.in, eventToMessage.out)
        })
}
