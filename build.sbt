organization := "org.srethink"

name := "srethink"

version := "0.0.9-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val nettyV = "4.0.28.Final"
  val circeV = "0.2.0"
  Seq(
    "io.circe"          %% "circe-core"         % circeV,
    "io.circe"          %% "circe-generic"      % circeV,
    "io.circe"          %% "circe-parse"        % circeV,
    "org.slf4j"         % "slf4j-api"           % "1.7.12",
    "io.netty"          % "netty-transport"     % nettyV,
    "io.netty"          % "netty-codec"         % nettyV,
    "org.scalatest"     %% "scalatest"          % "2.2.4"      % "test",
    "org.slf4j"         % "slf4j-simple"        % "1.7.12"     % "test")
}

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-language:implicitConversions",
  "-language:dynamics",
  "-language:higherKinds",
  "-language:existentials",
  "-language:reflectiveCalls"
)

publishArtifact in Test := false

parallelExecution in Global := false
