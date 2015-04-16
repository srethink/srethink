organization := "org.srethink"

name := "srethink"

version := "0.0.9-SNAPSHOT"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.4")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies <<= scalaVersion{ scalaVersion =>
  val Some(majorV) = CrossVersion.partialVersion(scalaVersion)
  val playV = "2.3.4"
  val nettyV = "3.9.3.Final"
  Seq(
    "io.netty"          % "netty"         % nettyV,
    "org.scala-lang"    % "scala-compiler"% scalaVersion,
    "com.typesafe.play" %% "play-json"    % playV % "provided",
    "org.slf4j"         % "slf4j-api"     % "1.7.7" % "provided",
    "org.specs2"        %% "specs2"       % "2.3.13" % "test",
    "org.slf4j"         % "slf4j-simple"  % "1.7.7" % "test"
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
