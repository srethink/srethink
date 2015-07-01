organization := "org.srethink"

name := "srethink"

version := "0.0.9-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= {
  val nettyV = "4.0.28.Final"
  Seq(
    "io.netty"          % "netty-transport"     % nettyV,
    "io.netty"          % "netty-codec"         % nettyV
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
