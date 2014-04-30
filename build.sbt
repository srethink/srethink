organization := "srethink"

name := "srethink"

version := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "io.netty" % "netty" % "3.9.0.Final",
  "net.sandrogrzicic" %% "scalabuff-runtime" % "1.3.7",
  "org.scalamacros" %% "quasiquotes" % "2.0.0",
  "org.specs2" %% "specs2" % "2.3.11" % "test")

scalacOptions ++= Seq("-feature", "-deprecation")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.0" cross CrossVersion.full)
