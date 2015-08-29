package org.srethink.net

import org.scalatest._
import org.scalatest.concurrent._
import scala.concurrent.duration._
import scala.concurrent._

trait RethinkSpec extends FlatSpec with Matchers with ScalaFutures with OneInstancePerTest with BeforeAndAfter {
  val config = new NettyConnectionConfig()
  val conn = new NettyConnection(config)
  implicit val p = PatienceConfig(timeout = 3.seconds)

  before {
    println("Opening connect...")
    Await.ready(conn.connect(), Duration.Inf)
  }

  after {
    println("Closing connection...")
    Await.ready(conn.close(), Duration.Inf)
  }
}
