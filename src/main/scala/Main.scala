package net.davidwiles.templater

import scala.util.Try

object Main {
  def main(args: Array[String]): Unit = {
    Try(TemplateEngine.start(args.toList)).recover {
      case ex: Exception => println(s"Fatal error: ${ex.getMessage}")
    }
  }
}
