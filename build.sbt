organization := "org.srethink"

name := "srethink"

scalaVersion := "2.11.11"

libraryDependencies ++= {
  val nettyV = "4.0.49.Final"
  val circeV = "0.8.0"
  Seq(
    "org.typelevel"     %% "cats"               % "0.9.0",
    "io.circe"          %% "circe-core"         % circeV,
    "io.circe"          %% "circe-generic"      % circeV,
    "io.circe"          %% "circe-parser"       % circeV,
    "org.slf4j"         % "slf4j-api"           % "1.7.21",
    "io.netty"          % "netty-transport"     % nettyV,
    "io.netty"          % "netty-codec"         % nettyV,
    "io.netty"          % "netty-handler"       % nettyV,
    "org.scalatest"     %% "scalatest"          % "3.0.3"      % "test",
    "org.slf4j"         % "slf4j-simple"        % "1.7.21"     % "test")
}

crossScalaVersions := Seq("2.11.11", "2.12.3")

scalacOptions ++= compilerOptions(scalaVersion.value)

def compilerOptions(version: String) = {
  if (version.startsWith("2.11")) {
    Seq(
      "-Ywarn-unused-import",
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
  } else {
    Seq(
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
  }
}

publishArtifact in Test := false

parallelExecution in Global := false

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

fork in test := true
