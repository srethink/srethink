package srethink.api

import srethink.net._

trait ConnectionManager {

  def get(): Connection

  def config: RethinkConfig

  def close()
}

class NettyConnectionManager(val config : NettyRethinkConfig) extends ConnectionManager {
  @volatile private var connection: Option[Connection] = None

  def get() = {
    if(isConnected) {
      connection.get
    } else {
      synchronized {
        if(isConnected) {
          connection.get
        } else {
          val newConnection = new NettyConnection(config)
          newConnection.connect()
          connection = Some(newConnection)
          connection.get
        }
      }
    }
  }

  private def isConnected = {
    (for(c <- connection if c.isConnected) yield true).getOrElse(false)
  }

  def close() {
    synchronized {
      if(isConnected) {
        connection.foreach(_.close())
      }
    }
  }
}
