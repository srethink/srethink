organization := "org.srethink"

name := "srethink"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.2")

libraryDependencies <<= scalaVersion{ scalaVersion =>
  val Some(majorV) = CrossVersion.partialVersion(scalaVersion)
  val shared = Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion,
    "io.netty" % "netty" % "3.9.0.Final",
    "net.sandrogrzicic" %% "scalabuff-runtime" % "1.3.8",
    "org.specs2" %% "specs2" % "2.3.13" % "test")
  majorV match {
    case (2, 10) =>
      shared ++ Seq(
        compilerPlugin("org.scalamacros" % "paradise" % "2.0.0" cross CrossVersion.full),
        "org.scalamacros" %% "quasiquotes" % "2.0.0")
    case (2, 11) =>
      shared
  }
}

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-language:implicitConversions",
  "-language:dynamics",
  "-language:higherKinds"
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
