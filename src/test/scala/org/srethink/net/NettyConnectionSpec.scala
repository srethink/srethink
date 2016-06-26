package org.srethink.net

import org.scalatest._
import org.scalatest.concurrent._

class NettyConnectionSpec extends RethinkSpec with Assertions {

  val conn = rdb.config.connectionFactory.get()

  "netty connection" should "connect to server" in {
    conn.connect().map(_ => succeed)
  }

  it should "send query then receive response" in {
    val query = Message(1, "[1, 59]")
    conn.execute(query).map(_.token shouldEqual(query.token))
  }
}
