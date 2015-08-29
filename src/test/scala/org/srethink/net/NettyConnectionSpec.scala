package org.srethink.net

import org.scalatest._
import org.scalatest.concurrent._

class NettyConnectionSpec extends RethinkSpec {

"netty connection" should "connect to server" in {
    whenReady(conn.connect()) { success =>
      success shouldBe true
    }
  }

  it should "send query then receive response" in {
    val query = Message(1, "[1, 59]")
    whenReady(conn.execute(query)) { resp =>
      resp.token shouldBe query.token
    }
  }
}
