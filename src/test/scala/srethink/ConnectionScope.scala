package srethink

import org.specs2.specification._
import srethink.api._
import srethink.net._

trait Connected {

  implicit lazy val queryExecutor = {
    val cfg = RethinkConfig.nettyConfig(
      version = RethinkConfig.V2)
    val connectionManager = new NettyConnectionManager(cfg)
    new ManagedQueryExecutor(connectionManager)
  }


  def disconnect() = queryExecutor.close()
}

trait WithConnection extends Connected  with Scope {
}
