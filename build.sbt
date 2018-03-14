organization := "org.srethink"

name := "srethink"

version := "0.0.22-2.4"

scalaVersion := "2.11.12"

crossScalaVersions := Seq("2.11.12")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val playV = "2.4.6"
  val nettyV = "3.10.6.Final"
  Seq(
    "io.netty"          % "netty"         % nettyV,
    "co.fs2"            %% "fs2-core"     % "0.9.5",
    "com.typesafe.play" %% "play-json"    % playV % "provided",
    "org.specs2"        %% "specs2"       % "2.3.13" % "test",
    "org.slf4j"         % "slf4j-api"     % "1.7.22" % "provided",
    "org.slf4j"         % "slf4j-simple"  % "1.7.22" % "test"
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

ScoverageKeys.minimumCoverage := 80

ScoverageKeys.failOnMinimumCoverage := true

ScoverageKeys.highlighting := {
  if (scalaBinaryVersion.value == "2.10") false
  else false
}

publishArtifact in Test := false

parallelExecution in Global := false

instrumentSettings

coverallsSettings

ScoverageKeys.excludedPackages in ScoverageCompile := "srethink\\.protocol\\..*"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
