package org.srethink.net

import org.srethink._
import org.scalatest._

trait RethinkSpec extends AsyncFlatSpec with Matchers {
  lazy val rdb = RethinkSpec.rdb
}

object RethinkSpec {
  val rdb = r.executor()
}
