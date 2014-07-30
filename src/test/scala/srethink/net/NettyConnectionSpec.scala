package srethink.net

import org.specs2.mutable.Specification
import org.specs2.specification._
import srethink.protocol._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.immutable._
import srethink._
import srethink.api._

class NettyConnectionSpec extends Specification {

  def createDb(name: String) = Query(
    `type` = Some(Query.QueryType.START),
    query = Some(DBCreate(name).toTerm),
    token = Some(TokenGenerator.nextToken)
  )

  "connection" should  {
    "connect to server" in new srethink.WithConnection {
      //wait connection success
      queryExecutor.connectionManager.get.isConnected must beTrue
    }

    "failure after disconnect" in new srethink.WithConnection with TermQuery {
      val conn = queryExecutor.connectionManager.get
      conn.isConnected must beTrue
      conn.close()
      val fooFut = conn.query(createDb("foo")).failed
      val barFut = conn.query(createDb("bar")).failed
      for {
        f <- fooFut
        b <- barFut
      } yield (f must beAnInstanceOf[Exception]) and (b must beAnInstanceOf[Exception])
    }
  }
}
