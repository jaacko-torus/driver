package com.jaackotorus

import com.typesafe.config
import scopt.OParser

import java.net.URI
import scala.util.{Failure, Success, Try}

object Program {
  val conf: config.Config = config.ConfigFactory.load("application")

  def main(args: Array[String]): Unit = {
    val builder = OParser.builder[Config]

    val parser = {
      import builder._
      OParser.sequence(
        programName("driver"),
        head("driver", "0.1.0"),
        version('v', "version"),
        help('h', "help"),
        opt[String]("interface")
          .abbr("i")
          .action((interface, c) => c.copy(interface = interface))
          .validate(interface =>
            if (isValidIpV4(interface)) {
              success
            } else {
              failure(s"Interface \"${interface}\" is not a valid a valid interface address")
            }
          )
          .text("(default: localhost) interface address (e.x.: 0.0.0.0, localhost, 127.0.0.1)"),
        opt[String]("client-source")
          .abbr("cs")
          .action((client_source, c) => c.copy(client_source = client_source))
          .validate(client_source =>
            if (isValidURI(client_source)) {
              success
            } else {
              failure(s"Client source \"${client_source}\" is not a valid URI")
            }
          )
          .text(
            "(default: src/main/resources/client) directory to be considered the client root. It should have an `index.html` file inside"
          ),
        opt[Int]("port-http")
          .abbr("ph")
          .action((port_http, c) => c.copy(port_http = port_http))
          .text("(default: 80) port for the client HTTP service (e.x.: 80, 8080, 9000)"),
        opt[Int]("port-ws")
          .abbr("pw")
          .action((port_ws, c) => c.copy(port_ws = port_ws))
          .text("(default: 8081) port for the client WS service"),
        opt[Boolean]("interactive")
          .action((interactive, c) => c.copy(interactive = interactive))
          .text("(default: false) server in interactive mode")
      )
    }

    OParser.parse(parser, args, Config()) match {
      case Some(config) =>
        Server.run(config)
      case _ =>
    }
  }

  def isValidURI(string: String): Boolean = {
    Try(new URI(string)) match {
      case Success(_) => true
      case Failure(_) => false
    }
  }

  def isValidIpV4(string: String): Boolean = {
    val n = raw"((1?)(\d?)\d|2[0-4]\d|25[0-5])".r
    (raw"($n\.){3}$n|localhost").r.matches(string)
  }

  case class Config(
      interface: String = conf.getString("driver.interface"),
      client_source: String = conf.getString("driver.client-source"),
      port_http: Int = conf.getInt("driver.port.http"),
      port_ws: Int = conf.getInt("driver.port.ws"),
      interactive: Boolean = conf.getBoolean("driver.interactive")
  )
}
