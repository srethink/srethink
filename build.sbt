organization := "org.srethink"

name := "srethink"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val nettyV = "4.0.37.Final"
  val circeV = "0.7.0"
  Seq(
    "org.typelevel"     %% "cats"               % "0.9.0",
    "io.circe"          %% "circe-core"         % circeV,
    "io.circe"          %% "circe-generic"      % circeV,
    "io.circe"          %% "circe-parser"       % circeV,
    "org.slf4j"         % "slf4j-api"           % "1.7.21",
    "io.netty"          % "netty-transport"     % nettyV,
    "io.netty"          % "netty-codec"         % nettyV,
    "io.netty"          % "netty-handler"       % nettyV,
    "org.scalatest"     %% "scalatest"          % "3.0.0"      % "test",
    "org.slf4j"         % "slf4j-simple"        % "1.7.21"     % "test")
}

scalacOptions ++= Seq(
  "-Ywarn-unused-import",
  "-Ybackend:GenBCode",
  "-Xfuture",
  "-Ywarn-dead-code",
  "-Ydelambdafy:method",
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
