organization := "org.srethink"

name := "srethink"

scalaVersion := "2.12.18"

def circeV(scalaV: String) = {
  if(scalaV.startsWith("2.11")) {
    "0.11.1"
  } else "0.12.3"
}

libraryDependencies ++= {
  val nettyV = "4.1.101.Final"
  Seq(
    "org.typelevel"     %% "cats-free"          % "2.6.1",
    "org.typelevel"     %% "cats-effect"        % "2.5.4",
    "co.fs2"            %% "fs2-core"           % "2.5.10",
    "io.circe"          %% "circe-core"         % circeV(scalaVersion.value),
    "io.circe"          %% "circe-generic"      % circeV(scalaVersion.value),
    "io.circe"          %% "circe-parser"       % circeV(scalaVersion.value),
    "org.slf4j"         % "slf4j-api"           % "1.7.21",
    "io.netty"          % "netty-transport"     % nettyV,
    "io.netty"          % "netty-codec"         % nettyV,
    "io.netty"          % "netty-handler"       % nettyV,
    "org.scalatest"     %% "scalatest"          % "3.0.0"      % "test",
    "org.slf4j"         % "slf4j-simple"        % "1.7.21"     % "test")
}

scalacOptions ++= Seq(
  "-Ywarn-unused-import",
  "-Xfuture",
  "-Ywarn-dead-code",
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

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

fork in test := true
ThisBuild / evictionErrorLevel := Level.Warn
