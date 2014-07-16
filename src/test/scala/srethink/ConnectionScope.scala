package srethink

import org.specs2.specification._
import srethink.api._
import srethink.net._

trait Connected {

  implicit lazy val queryExecutor = Connected.queryExecutor


  def disconnect() = queryExecutor.close()
}

object Connected {
  lazy val queryExecutor = {
    val cfg = RethinkConfig.nettyConfig(
      version = RethinkConfig.V2)
    val connectionManager = new NettyConnectionManager(cfg)
    new ManagedQueryExecutor(connectionManager)
  }
}

trait WithConnection extends Connected  with Scope {
}
