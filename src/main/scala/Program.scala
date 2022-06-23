package com.jaackotorus

import com.typesafe.config.{Config, ConfigFactory}
import scopt.OParser

import java.io.File
import java.net.URI
import scala.util.{Failure, Success, Try}

object Program {
  val conf = ConfigFactory.load("application.conf") // .parseFile(new File("src/main/resources/application.conf"))
  println(conf)

  case class Config(
      interface: String = "localhost",
      client_source: String = "src/main/resources/client"
  )

  def main(args: Array[String]): Unit = {
    val builder = OParser.builder[Config]

    val parser = {
      import builder._
      OParser.sequence(
        programName("driver"),
        head("driver", "0.1.0"),
        opt[String]("interface")
          .abbr("i")
          .action((interface, c) => c.copy(interface = interface))
          .validate(value =>
            if (isValidIpV4(value)) {
              success
            } else {
              failure("Interface is improperly formatted.")
            }
          ),
        opt[String]("client-source")
          .abbr("cs")
          .action((client_source, c) => c.copy(client_source = client_source))
          .validate(value =>
            if (isValidURI(value)) {
              success
            } else {
              failure("Client source must be a valid URI")
            }
          )
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
}
