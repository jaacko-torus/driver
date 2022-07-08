package com.jaackotorus

import com.typesafe.config
import scopt.OParser

import java.net.URI
import scala.util.{Failure, Success, Try}

object Program {
  def main(args: Array[String]): Unit = {
    val builder = OParser.builder[CLIConfig]

    val parser = {
      import builder._
      OParser.sequence(
        programName("driver"),
        head("driver", "0.1.0"),
        version('v', "version"),
        help('h', "help"),
        opt[Int]("port-http")
          .action((port_http, c) => c.copy(port_http = port_http))
          .text(s"(default: ${conf.port.http}) port for the client HTTP service (e.x.: 80, 8080, 9000)"),
        opt[Int]("port-ws")
          .action((port_ws, c) => c.copy(port_ws = port_ws))
          .text(s"(default: ${conf.port.ws}) port for the client WS service"),
        opt[String]("interface")
          .abbr("i")
          .action((interface, c) => c.copy(interface = interface))
          .validate(interface =>
            if (isValidIpV4(interface)) {
              success
            } else {
              failure(s"Interface \"$interface\" is not a valid a valid interface address")
            }
          )
          .text(s"(default: ${conf.interface}) interface address (e.x.: 0.0.0.0, localhost, 127.0.0.1)"),
        opt[String]("client-source")
          .abbr("cs")
          .action((client_source, c) => c.copy(client_source = client_source))
          .validate(client_source =>
            if (isValidURI(client_source)) {
              success
            } else {
              failure(s"Client source \"$client_source\" is not a valid URI")
            }
          )
          .text(
            s"(default: ${conf.client_source}) directory to be considered the client root. It should have an `index.html` file inside"
          ),
        opt[Boolean]("interactive")
          .action((interactive, c) => c.copy(interactive = interactive))
          .text(s"(default: ${conf.interactive}) server in interactive mode"),
        opt[Boolean]("localhost")
          .action((localhost, c) =>
            if (localhost) {
              c.copy(
                port_http = conf.port.http,
                port_ws = conf.port.ws,
                interface = conf.interface,
                client_source = conf.client_source,
                interactive = conf.interactive
              )
            } else {
              c
            }
          )
          .text(s"(default: ${conf.localhost}) use localhost settings")
      )
    }

    OParser.parse(parser, args, CLIConfig()) match {
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

  object conf {
    private val app_conf: config.Config = config.ConfigFactory.load("application")

    object port {
      val http: Int = app_conf.getInt("driver.port.http")
      val ws: Int = app_conf.getInt("driver.port.ws")
    }

    val interface: String = app_conf.getString("driver.interface")
    val client_source: String = app_conf.getString("driver.client-source")
    val interactive: Boolean = app_conf.getBoolean("driver.interactive")
    val localhost: Boolean = app_conf.getBoolean("driver.localhost")
  }

  case class CLIConfig(
      port_http: Int = conf.port.http,
      port_ws: Int = conf.port.ws,
      interface: String = conf.interface,
      client_source: String = conf.client_source,
      interactive: Boolean = conf.interactive
  )
}
