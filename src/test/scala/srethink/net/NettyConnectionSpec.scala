package srethink.net

import org.specs2.mutable.Specification
import srethink._

class NettyConnectionSpec extends PlayRethinkSpec {

  "netty connection" should {
    "close connection" in {
      val connection = executor.factory.acquire()
      connection.close()
      connection.close()
      true
    }
    "throw error after close" in {
      val connection = executor.factory.acquire()
      connection.close()
      val query = r.db("library").table("book")
      val fut = connection.query(Query(9999L, stringify(query.term)))
      fut.failed.map{ ex => ex.printStackTrace(); true }.await(10)
    }
  }
}
