package org.srethink.net

import org.srethink._
import org.scalatest._
import org.scalatest.concurrent._
import scala.concurrent.duration._
import scala.concurrent._

trait RethinkSpec extends AsyncFlatSpec with Matchers {
  lazy val rdb = r.executor()
}
