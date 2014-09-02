package srethink.net

import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext
import srethink.json._
import srethink.protocol.QueryConstant._

trait QueryExecutor {
  val tokenRegistry = TrieMap[Long, Connection]()
  val factory: ConnectionFactory
  val token = new AtomicLong()

  implicit val executionContext: ExecutionContext

  final def start(query: String) = {
    val c = factory.acquire()
    val t = token.incrementAndGet()
    tokenRegistry.put(t, c)
    c.query(Query(t, query))
  }

  final def complete(token: Long) {
    tokenRegistry.remove(token)
  }

  final def continue(token: Long, query: String) = {
    tokenRegistry.get(token).foreach { c =>
      c.query(Query(token, query))
    }
  }

  final def stop(token: Long, query: String) = {
    tokenRegistry.get(token).foreach { c =>
      c.query(Query(token, query))
    }
  }
}

class NettyQueryExecutor(config: NettyRethinkConfig) extends QueryExecutor {
  val executionContext = config.executionContext
  val factory = new PooledConnectionFactory(config.poolSize)({
    new NettyConnection(config)
  })
}
