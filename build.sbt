organization := "org.srethink"

name := "srethink"

version := "0.0.9-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val nettyV = "4.0.28.Final"
  Seq(

    "org.slf4j"         % "slf4j-api"           % "1.7.12",
    "io.netty"          % "netty-transport"     % nettyV,
    "io.netty"          % "netty-codec"         % nettyV,
    "org.scalatest"     % "scalatest_2.11"      % "2.2.4"      % "test",
    "org.slf4j"         % "slf4j-simple"        % "1.7.12"     % "test"

  )
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
