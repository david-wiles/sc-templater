package net.davidwiles.templater

import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.io.Source
import scala.language.implicitConversions
import scala.jdk.CollectionConverters._

object ImplicitConversions {
  private val tmplRe = "\\{\\{\\s*template\\s*\"([.0-9a-zA-Z/\\-]+)\"\\s*}}".r
  private val varRe = "\\{\\{\\s*\"([.0-9a-zA-Z/\\-]+)\"\\s*}}".r

  class TextWithSubstitution(text: String) {
    /**
     * Replaces all of the templates in the text with the text found in those files.
     * If there were template replacements, then Some(String) is returned. Otherwise,
     * a None will be returned, indicating the template replacement is finished
     *
     * @param base  The base directory containing templates
     * @return The new text after replacements
     */
    def replaceTemplates(base: String): String = {
      tmplRe.findFirstMatchIn(text).map { m =>
        val result = text.take(m.start) + readTemplate(Paths.get(base, m.group(1)).toString) + text.drop(m.end)
        result.replaceTemplates(base)
      }.getOrElse(text)
    }

    /**
     * Replace variables using the key-value map. The resulting string is
     * returned if there were replacements, or None otherwise
     *
     * @param vars  The key-value pairs to use during substitution
     * @return      The new text after replacement
     */
    def replaceVariables(vars: Map[String, String]): String = {
      varRe.findFirstMatchIn(text).map { m =>
        val result = text.take(m.start) + vars(m.group(1)) + text.drop(m.end)
        result.replaceVariables(vars)
      }.getOrElse(text)
    }
  }

  class Variables(variables: mutable.Map[String, String]) {
    type KVPair = Tuple2[String, String]

    def addFromFile(filename: String, namespace: String = ""): Unit = {
      val source = Source.fromFile(filename)
      variables.addAll {
        source.getLines().flatMap { line =>
          parseKVPair(line, namespace) match {
            case Left(err) =>
              println(err)
              None
            case Right(value) => Some(value)
          }
        }
      }
      source.close()
    }

    private def parseKVPair(text: String, ns: String = ""): Either[String, KVPair] = {
      val namespace = if (ns == "" || ns.endsWith(".")) ns else s"$ns."
      text.split('=').toList match {
        case key :: value :: Nil => Right((s"$namespace$key", value))
        case _ => Left(s"Invalid key value pair provided: $text")
      }
    }
  }

  implicit def mapToVariables(variables: mutable.Map[String, String]): Variables =
    new Variables(variables)

  implicit def stringToTextWithSubstitution(text: String): TextWithSubstitution =
    new TextWithSubstitution(text)

  /**
   * Reads all text from a file into a string. No attempts are made to catch File exceptions, this
   * assumes that file validation has already been done properly by the caller.
   * This should be identical to Files.readString, which is not available with Scala native 0.4.3
   *
   * @param filename  Path to the file to read
   * @return          The file's contents as a String
   */
  def readTemplate(filename: String): String = Files.readAllLines(Paths.get(filename)).asScala.mkString("\n")
}
