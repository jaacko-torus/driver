package com.jaackotorus

import scopt.OParser

object Program {
  case class Config(interface: String = "localhost")

  def main(args: Array[String]): Unit = {
    val builder = OParser.builder[Config]

    val parser = {
      import builder._
      OParser.sequence(
        programName("mill driver"),
        head("driver", "0.1.0"),
        opt[String]('i', "interface")
          .action((i, c) => c.copy(i))
          .validate(value =>
            if (isIpV4(value)) success
            else failure("Interface is improperly formatted.")
          )
      )
    }

    OParser.parse(parser, args, Config()) match {
      case Some(config) =>
        Server.run(config)
      case _ =>
    }
  }

  def isIpV4(string: String): Boolean = {
    val n = raw"((1?)(\d?)\d|2[0-4]\d|25[0-5])".r
    (raw"($n\.){3}$n|localhost").r.matches(string)
  }
}
