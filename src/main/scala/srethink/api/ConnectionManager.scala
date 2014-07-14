package srethink.api

import srethink.net._

trait ConnectionManager {

  def get(): Connection

  def config: RethinkConfig

  def close()
}

class NettyConnectionManager(val config : NettyRethinkConfig) extends ConnectionManager {
  @volatile private var connection: Connection = {
    synchronized {
      val c = new NettyConnection(config)
      c.connect()
      c
    }
  }

  def get() = {
    if(connection.isConnected) {
      connection
    } else {
      synchronized {
        if(connection.isConnected) {
          connection
        } else {
          val newConnection = new NettyConnection(config)
          newConnection.connect()
          connection = newConnection
          connection
        }
      }
    }
  }

  def close() {
    synchronized {
      if(connection.isConnected) {
        connection.close()
      }
    }
  }
}
