package srethink.net

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger
import java.util.Date


trait ConnectionFactory {
  def acquire(): Connection
}

class PooledConnectionFactory(size: Int)(factoryMethod: => Connection)
    extends ConnectionFactory {

  val connections = new ConcurrentHashMap[Int, Connection]
  /*
   * Replace the old connection, do nothing if is replaced by another thread
   * @returns replaced connection
   */
  private def replace(id: Int, oldV: Connection) = {
    val newV = factoryMethod
    if(connections.replace(id, oldV, newV)) {
      newV.connect()
      newV
    } else {
      connections.get(id)
    }
  }

  private def offer(id: Int) = {
    val newV = factoryMethod
    val oldV = connections.putIfAbsent(id, newV)
    if(oldV == null) {
      newV.connect()
      newV
    } else {
      oldV
    }
  }

  private def getActive(id: Int) = {
    val present = connections.get(id)
    if(present == null) {
      offer(id)
    } else if(present.isConnected) {
      present
    } else {
      replace(id, present)
    }
  }

  def acquire() = {
    val id = scala.util.Random.nextInt(size)
    getActive(id)
  }
}
