package net.davidwiles.templater

import net.davidwiles.commands._
import net.davidwiles.templater.ImplicitConversions._

import java.io.{File, FileOutputStream, OutputStream}
import java.nio.file.{Files, Paths}
import scala.annotation.tailrec
import scala.collection.mutable
import scala.io.Source
import scala.util.{Failure, Success, Try}

class TemplateEngine(template: String, output: OutputStream = System.out, variables: Map[String, String] = Map()) extends Command {
  private val base: String = Paths.get(template).getParent.toString

  override def execute(): Unit = {
    output.write {
      readTemplate(template)
        .replaceTemplates(base)
        .replaceVariables(variables)
        .getBytes
    }
  }
}

object TemplateEngine extends CommandParser {

  type KVPair = Tuple2[String, String]

  val spec: String =
    """
      |This template engine resembles the Go template engine. To replace
      |a section with another template file, use
      |
      |{{ template "<filename>" }}
      |
      |To use a variable defined in the variable file or via command-line
      |arguments, use
      |
      |{{ "<key" }}
      |
      |to replace it with the value associated with the key. If the key is
      |not found, it will be replaced with an empty string.
      |""".stripMargin

  override val commandName: String = "templater"

  override val usage: String =
    s"""
       |Usage: $commandName [OPTIONS] TEMPLATE
       |
       |A simple template engine.
       |
       |This is a straightforward template engine which is able to build
       |a string of text from a given template and variables. The template
       |engine will also search for templates recursively.
       |
       |Options:
       |  -h, --help       Print this message and exit
       |  --explain        Print the template specification and exit
       |  -of, --out-file  The output file to write to on completion. Output
       |                   will be written to stdout by default
       |  -e, --env-var    Add the key-value pair to use within the template.
       |                   The pair should be separated by '='
       |  -vf, --var-file  Use the variables specified in the given file. All
       |                   variables should be given in the format of
       |                   'key=value' separated by newlines.
       |
       |Arguments:
       |  TEMPLATE  The entrypoint template file. This can be any text file
       |            using the format specified by --explain
       |""".stripMargin

  override def parse(args: List[String]): Option[Command] = {
    var outStream: OutputStream = System.out
    val variables: mutable.Map[String, String] = mutable.Map.empty[String, String]

    @tailrec
    def parseMore(list: List[String]): Option[Command] = {
      list match {
        case ("-h" | "--help") :: tail => printUsage()
        case "--explain" :: tail => printUsage(spec)
        case ("-of" | "--out-file") :: of :: tail =>
          Try(new FileOutputStream(of)) match {
            case Failure(exception) => printUsage(exception.getMessage)
            case Success(value) =>
              outStream = value
              parseMore(tail)
          }
        case ("-e" | "--env-var") :: kv :: tail =>
          parseKVPair(kv) match {
            case Left(err) => printUsage(err)
            case Right(value) =>
              variables.addOne(value)
              parseMore(tail)
          }
        case ("-vf" | "--var-file") :: vf :: tail =>
          Try {
            val p = Paths.get(vf)
            Files.exists(p) && Files.isRegularFile(p)
          } match {
            case Failure(exception) => printUsage(exception.getMessage)
            case Success(false) => printUsage("The variable file must correspond to a valid filename")
            case Success(true) =>
              val source = Source.fromFile(vf)
              variables.addAll {
                source.getLines().flatMap { line =>
                  parseKVPair(line) match {
                    case Left(err) =>
                      println(err)
                      None
                    case Right(value) => Some(value)
                  }
                }
              }
              source.close()

              parseMore(tail)
          }
        case template :: Nil =>
          Try {
            val p = Paths.get(template)
            Files.exists(p) && Files.isRegularFile(p)
          } match {
            case Failure(exception) => printUsage(exception.getMessage)
            case Success(false) => printUsage("Must provide a valid file for the template")
            case Success(true) => Some(TemplateEngine(template, outStream, variables.toMap))
          }
        case _ => printUsage()
      }
    }

    parseMore(args)
  }

  override def parse(args: List[String], persistentArgs: Map[String, Any]): Option[Command] = parse(args)

  def apply(template: String, output: OutputStream, variables: Map[String, String]) =
    new TemplateEngine(template, output, variables)

  private def parseKVPair(text: String): Either[String, KVPair] = {
    text.split('=').toList match {
      case key :: value :: Nil => Right((key, value))
      case _ => Left(s"Invalid key value pair provided: $text")
    }
  }
}
