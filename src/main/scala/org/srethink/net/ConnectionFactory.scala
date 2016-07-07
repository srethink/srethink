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
  lazy val connRef = new AtomicReference(newConnection)
  implicit val ec = org.srethink.exec.trampoline

  def newConnection: NettyConnection = {
    val conn = new NettyConnection(config)
    conn.connect()
    conn
  }

  def get() = {
    logger.debug("[Look up connections]")
    val curr = connRef.get()
    curr.closed.map {
      case true =>
        logger.info("Detected closed connection...")
        putNewConn(curr, connRef)
      case false =>
        logger.debug("Offer active connection {}", curr)
        curr
    }.recover {
      case _: Throwable => putNewConn(curr, connRef)
    }
  }

  private def putNewConn(curr: NettyConnection, ref: AtomicReference[NettyConnection]) = {
    val newConn = new NettyConnection(config)
    if(connRef.compareAndSet(curr,  newConn)) {
      logger.info("Create new connection...")
      newConn.connect()
      newConn
    } else connRef.get()
  }
}
