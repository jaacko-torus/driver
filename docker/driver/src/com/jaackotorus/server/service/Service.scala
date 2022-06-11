package com.jaackotorus.server.service

import akka.http.scaladsl.server.Route
import akka.stream.Shape

case class Service[S <: Shape](
    interface: String,
    port: Int,
    route: (Any => S) => Route
)
