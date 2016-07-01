package org.srethink.net

import org.scalatest._

class NettyConnectionSpec extends AsyncFlatSpec with Matchers with Assertions with BeforeAndAfterAll {

  val conn = new NettyConnection(config = new NettyConnectionConfig)

  override def beforeAll(): Unit = {
    conn.connect()
  }

  val query = Message(1, "[1, 59]")

  "netty connection" should "ok to connect multi-times" in {
    conn.connect().map(_ => succeed)
  }

  it should "send query then receive response" in {

    conn.execute(query).map(_.token shouldEqual(query.token))
  }

  it should "report error after close" in {
    val conn = new NettyConnection(config = new NettyConnectionConfig)
    conn.connect()
    conn.execute(query).map(_ => succeed)
    conn.close()
    conn.closed.map(_ shouldEqual(true))
    recoverToSucceededIf[java.nio.channels.ClosedChannelException] {
      conn.execute(query)
    }
  }

  override def afterAll(): Unit = {
    conn.close()
  }
}
