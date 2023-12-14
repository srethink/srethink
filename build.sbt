organization := "org.srethink"

name := "srethink"

scalaVersion := "2.11.12"

crossScalaVersions := Seq("2.11.12", "2.12.18", "2.13.12")

def playJsonV(sv: String) = {
  if (sv.startsWith("2.11"))
    "2.5.19"
  else "2.10.3"
}
val slf4jVersion = "1.7.36"

libraryDependencies ++= {
  val nettyV = "3.10.6.Final"
  Seq(
    "io.netty"           % "netty"     % nettyV,
    "co.fs2"            %% "fs2-core"  % "2.0.0",
    "org.slf4j"          % "slf4j-api" % slf4jVersion,
    "com.typesafe.play" %% "play-json" % playJsonV(
      scalaVersion.value
    )                % Provided,
    "org.scalameta" %% "munit"        % "1.0.0-M10"  % Test,
    "org.slf4j"      % "slf4j-simple" % slf4jVersion % Test
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

parallelExecution in Global := false

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

ThisBuild / evictionErrorLevel := Level.Warn
