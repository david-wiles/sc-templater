ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

enablePlugins(ScalaNativePlugin)

libraryDependencies ++= Seq(
  "net.davidwiles" %%% "commands" % "0.1.1-SNAPSHOT",
  "org.scalatest" %%% "scalatest" % "3.2.14" % "test"
)

lazy val root = (project in file("."))
  .settings(
    name := "templater",
    organization := "net.davidwiles",
    idePackagePrefix := Some("net.davidwiles.templater")
  )
