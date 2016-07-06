package org.srethink.exec

import java.util.concurrent.atomic.AtomicReference
import org.srethink.net._
import org.slf4j._
import scala.concurrent.Future


trait ConnectionFactory {
  val config: NettyConnectionConfig
  def get(): Future[NettyConnection]
}

class AutoReconnectConnectionFactory(val config: NettyConnectionConfig) extends ConnectionFactory {

  val logger = LoggerFactory.getLogger(classOf[AutoReconnectConnectionFactory])
  val connRef = new AtomicReference(newConnection)
  implicit val ec = org.srethink.exec.trampoline

  def newConnection = {
    val conn = new NettyConnection(config)
    conn.connect()
    conn
  }

  def get() = {
    val curr = connRef.get()
    curr.closed.map {
      case true =>
        logger.info("Detected closed connection...")
        val newConn = new NettyConnection(config)
        if(connRef.compareAndSet(curr,  newConn)) {
          logger.info("Create new connection...")
          newConn.connect()
          newConn
        } else connRef.get()
      case false =>
        curr
    }
  }
}
