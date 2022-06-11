package com.jaackotorus.server.actor

import akka.actor._

import scala.collection.mutable

object ChatroomActor {
    case class User(name: String)
    case class `User&Actor`(user: User, actor: ActorRef)

    trait Event
    object Event {
        case class None() extends Event
        case class UserJoined(user: User, actor: ActorRef) extends Event
        case class UserLeft(username: String) extends Event
        case class UserSentMessage(username: String, message: String) extends Event
        case class UsersChanged(users: Iterable[User]) extends Event
    }
}

class ChatroomActor extends Actor {
    import ChatroomActor._

    val users: mutable.LinkedHashMap[String, `User&Actor`] =
        mutable.LinkedHashMap[String, `User&Actor`]()

    override def receive: Receive = {
        case Event.UserJoined(user, actor) => {
            users += (user.name -> `User&Actor`(user, actor))
            notifyUsersChanged()
        }
        case Event.UserLeft(username) => {
            users -= username
            notifyUsersChanged()
        }
        case message: Event.UserSentMessage => notifyUserSentMessage(message)
    }

    def notifyUserSentMessage(playerSentMessage: Event.UserSentMessage): Unit = {
        users.values.foreach(_.actor ! playerSentMessage)
    }

    def notifyUsersChanged(): Unit = {
        users.values.foreach(_.actor ! Event.UsersChanged(users.values.map(_.user)))
    }
}
