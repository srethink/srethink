package srethink.net

import fs2._
import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext
import srethink.json._
import srethink.protocol.QueryConstant._
import scala.concurrent.Future


trait QueryExecutor {
  val factory: ConnectionFactory
  val token = new AtomicLong()

  implicit val executionContext: ExecutionContext

  final def start(query: String): Future[Response] = {
    val t = token.incrementAndGet()
    val c = factory.acquire()
    start(c, t, query)
  }

  private def start(c: Connection, token: Long, query: String): Future[Response] = {
    val c = factory.acquire()
    c.query(Query(token, query))
  }

  final def continue(c: Connection, token: Long, query: String) = {
    c.query(Query(token, query))
  }

  final def cursor(query: String) = {
    val t = token.incrementAndGet()
    val c = factory.acquire()
    Stream.eval(start(c, t, query)) ++ Stream.repeatEval(continue(c, t, query))
  }

  final def stop(c: Connection, token: Long, query: String) = {
    c.query(Query(token, query))
  }
}

class NettyQueryExecutor(config: NettyRethinkConfig) extends QueryExecutor {
  val executionContext = config.executionContext
  val factory = new PooledConnectionFactory(config.poolSize)({
    new NettyConnection(config)
  })
}
