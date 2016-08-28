package org.srethink.net

import org.slf4j._
import org.scalatest._
import org.srethink._


trait RethinkSpec extends AsyncFlatSpec with Matchers {
  lazy val rdb = RethinkSpec.rdb
}

object RethinkSpec {
  val log = LoggerFactory.getLogger(classOf[RethinkSpec])
  log.info("Init rdb...")
  val rdb = r.executor()
  log.info(s"Created rdb instance $rdb")
}
