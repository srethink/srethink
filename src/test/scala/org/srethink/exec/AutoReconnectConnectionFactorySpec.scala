package org.srethink.exec

import org.scalatest._
import org.srethink.net._

class AutoReconnectConnectionFactorySpec extends AsyncFlatSpec with Matchers {
  val factory = new AutoReconnectConnectionFactory(config = new NettyConnectionConfig)
  "it" should "recoonect after conn closed" in {
    for {
      conn1 <- factory.get
      _ <- conn1.closed
      _ = conn1.close()
      conn2 <- factory.get()
      isConn2Closed <- conn2.closed
    } yield {
      conn1 should not equal(conn2)
      isConn2Closed should equal(false)
    }
  }

}
