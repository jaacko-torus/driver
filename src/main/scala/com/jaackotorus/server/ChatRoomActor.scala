package com.jaackotorus.server

import akka.actor.*

import scala.annotation.targetName

object ChatRoomActor {
    case class User(name: String)
    @targetName("User&Actor")
    case class `User&Actor`(user: User, actor: ActorRef)

    trait Event
    object Event:
        case class UserJoined(user: User, actor: ActorRef) extends Event
        case class UserLeft(username: String) extends Event
        case class UserSentMessage(username: String, message: String) extends Event
        case class UsersChanged(users: Iterable[User]) extends Event
}

class ChatRoomActor extends Actor {
    import ChatRoomActor.*

    val users = collection.mutable.LinkedHashMap[String, `User&Actor`]()

    override def receive: Receive = {
        case Event.UserJoined(user, actor)        => users += (user.name -> `User&Actor`(user, actor))
        case Event.UserLeft(username)             => users -= username
        case Event.UserSentMessage(user, message) =>
    }

    def notifyUsersChanged(): Unit = users.values.foreach(_.actor ! Event.UsersChanged(users.values.map(_.user)))
}
