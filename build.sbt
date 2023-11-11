ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "net.davidwiles" %% "commands" % "0.1.1-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "3.2.14" % "test"
)

lazy val root = (project in file("."))
  .enablePlugins(AssemblyInstallerPlugin)
  .settings(
    name := "tmpl",
    organization := "net.davidwiles",
  )
