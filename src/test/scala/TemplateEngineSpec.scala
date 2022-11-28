package net.davidwiles.templater

import net.davidwiles.templater.ImplicitConversions._

import org.scalatest._
import matchers._
import org.scalatest.flatspec.AnyFlatSpec

import java.nio.file.{Files, Paths}

class TemplateEngineSpec extends AnyFlatSpec with should.Matchers with BeforeAndAfterAll {

  "TemplateEngine.parse" should "return None when --help is used" in {
    assert(TemplateEngine.parse(List("--help")).isEmpty)
    assert(TemplateEngine.parse(List("-h")).isEmpty)
  }

  it should "return None when --explain is used" in {
    assert(TemplateEngine.parse(List("--explain")).isEmpty)
  }

  it should "call TemplateEngine with a template when a correct argument is passed" in {
    assert(TemplateEngine.parse(List("test_assets/template.txt")).nonEmpty)
  }

  it should "return None when an invalid template argument is passed" in {
    assert(TemplateEngine.parse(List("asdf")).isEmpty)
  }

  it should "call TemplateEngine with an outfile when --out-file is used" in {
    assert(TemplateEngine.parse(List("-of", "test_assets/output.txt", "test_assets/template.txt")).nonEmpty)
    assert(TemplateEngine.parse(List("--out-file", "test_assets/output.txt", "test_assets/template.txt")).nonEmpty)
  }

  it should "correctly add variables to the TemplateEngine" in {
    assert(TemplateEngine.parse(List("-e", "one=1", "-e", "two=2", "test_assets/template.txt")).nonEmpty)
    assert(TemplateEngine.parse(List("--env-var", "one=1", "--env-var", "two=2", "test_assets/template.txt")).nonEmpty)

  }

  it should "correctly add variables from a file" in {
    assert(TemplateEngine.parse(List("-vf", "test_assets/vars.txt", "test_assets/template.txt")).nonEmpty)
    assert(TemplateEngine.parse(List("--var-file", "test_assets/vars.txt", "test_assets/template.txt")).nonEmpty)
  }

  it should "write output that matches a reference" in {
    TemplateEngine.parse(List("-vf", "test_assets/vars.txt", "-of", "test_assets/output.txt", "test_assets/template.txt")).get
      .execute()

    assert(readTemplate("test_assets/output.txt").equals(readTemplate("test_assets/reference.txt")))
  }

  override def afterAll(): Unit = {
    Files.delete(Paths.get("test_assets/output.txt"))
  }
}
