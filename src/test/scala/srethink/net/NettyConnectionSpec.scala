package srethink.net

import cats.effect.IO
import org.specs2.mutable.Specification
import play.api.rql._
import scala.concurrent.duration._
import srethink._
import scala.concurrent.ExecutionContext.global

class NettyConnectionSpec extends RethinkSpec with org.specs2.time.NoTimeConversions {
  private implicit val cs = IO.contextShift(global)
  val testQuery =  {
    val term = """[1,[56,[[15,[[14,["library"]],"book"]],[2,[{"id":"1","title":"title1","author":"author1","coAuthors":[2,[]],"price":1.0,"quantity":1,"releaseDate":{"$reql_type$":"TIME","epoch_time":1428770257.372,"timezone":"+00:00"}},{"id":"2","title":"title2","author":"author2","coAuthors":[2,[]],"price":2.0,"quantity":2,"releaseDate":{"$reql_type$":"TIME","epoch_time":1428770257.372,"timezone":"+00:00"}}]]]],{}]"""
    Query(9999L, term)

  }

  def newConn(
    connTimeout: FiniteDuration = 3.seconds,
    requestTimeout: FiniteDuration = 3.seconds) = {
    val cfg = RethinkConfig.nettyConfig(
      requestTimeout = 1.millis,
      timer = new RethinkTimer(requestTimeout))
    new NettyConnection(cfg)
  }
  "netty connection" should {

    "close connection" in {
      val connection = newConn()
      connection.close()
      connection.close()
      true
    }

    "throw error after close" in {
      val connection = newConn()
      connection.connect()
      connection.close()
      val fut = connection.query(testQuery)
      fut.failed.map{ ex => ex.printStackTrace(); true }.await(10)
    }

    "perform request timeout" in {
      val c = newConn(3.seconds, 1.millis)
      c.connect()
      val fut = c.query(testQuery).map(_ => false).recover {
        case e: java.util.concurrent.TimeoutException =>
          e.printStackTrace()
          true
      }
      fut.await(10)
    }

    "perform connect timeout" in {
      val c = newConn(1.millis, 3.millis)
      try {
        c.connect()
      } catch {
        case e: Throwable =>
          e.printStackTrace
      }
      true
    }
  }
}
