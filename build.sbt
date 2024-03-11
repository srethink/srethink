organization := "org.srethink"

name := "srethink"

scalaVersion := "3.3.3"

crossScalaVersions := Seq("2.12.19", "2.13.13", "3.3.3")

libraryDependencies ++= {
  val nettyV = "4.1.101.Final"
  val circeV = "0.14.6"
  Seq(
    "org.typelevel" %% "cats-free"       % "2.10.0",
    "org.typelevel" %% "cats-effect"     % "3.5.4",
    "co.fs2"        %% "fs2-core"        % "3.9.4",
    "io.circe"      %% "circe-core"      % circeV,
    "io.circe"      %% "circe-generic"   % circeV,
    "io.circe"      %% "circe-parser"    % circeV,
    "org.slf4j"      % "slf4j-api"       % "2.0.6",
    "io.netty"       % "netty-transport" % nettyV,
    "io.netty"       % "netty-codec"     % nettyV,
    "io.netty"       % "netty-handler"   % nettyV,
    "org.scalatest" %% "scalatest"       % "3.2.12" % "test",
    "org.slf4j"      % "slf4j-simple"    % "2.0.6"  % "test"
  )
}

ThisBuild / libraryDependencySchemes += "org.typelevel" %% "cats-core" % "always"

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
