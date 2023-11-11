ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .enablePlugins(AssemblyInstallerPlugin)
  .settings(
    name := "tmpl",
    organization := "net.davidwiles",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % Test
  )
