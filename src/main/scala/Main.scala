package net.davidwiles.templater

import scala.util.{Failure, Success, Try}

object Main {
  def main(args: Array[String]): Unit = {
    Try(TemplateEngine.parse(args.toList)) match {
      case Success(Some(engine)) => engine.execute()
      case Failure(exception) => println(exception.getMessage)
      case _ => // Do nothing
    }
  }
}
