import sbt._
import Keys._
import scalabuff.ScalaBuffPlugin._

object SrethinkBuild extends Build {
  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ scalabuffSettings).configs(ScalaBuff)
}
