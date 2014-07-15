package srethink.api

import srethink._

class ConnectionManagerSpec extends TermSpec {

  val connectionManager = queryExecutor.connectionManager

  "connection manager" should {
    "reconnect while discount" in {
      val oldConnection = connectionManager.get()
      oldConnection.isConnected must beTrue
      connectionManager.close()
      oldConnection.isConnected must beFalse
      val newConnection = connectionManager.get()
      newConnection.isConnected must beTrue
    }
  }
}
