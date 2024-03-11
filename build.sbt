organization := "org.srethink"

name         := "srethink"
scalaVersion := "3.3.3"

crossScalaVersions := Seq("2.12.19", "2.13.13", "3.3.3")

val playJsonV    = "2.10.4"
val slf4jVersion = "2.0.12"
val fs2Version   = "3.9.4"
val nettyV       = "3.10.6.Final"

libraryDependencies ++= {
  Seq(
    "io.netty"           % "netty"        % nettyV,
    "org.slf4j"          % "slf4j-api"    % slf4jVersion,
    "co.fs2"            %% "fs2-core"     % fs2Version,
    "com.typesafe.play" %% "play-json"    % playJsonV    % Provided,
    "org.scalameta"     %% "munit"        % "1.0.0-M10"  % Test,
    "org.slf4j"          % "slf4j-simple" % slf4jVersion % Test
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
