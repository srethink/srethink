package org.srethink.net

import java.util.concurrent.atomic.AtomicReference

trait ConnectionFactory {
  val config: NettyConnectionConfig
  def get(): NettyConnection
}

class AutoReconnectConnectionFactory(val config: NettyConnectionConfig) extends ConnectionFactory {

  val connRef = new AtomicReference(newConnection)

  def newConnection = {
    val conn = new NettyConnection(config)
    conn.connect()
    conn
  }

  def get() = {
    val curr = connRef.get()
    curr.closed match {
      case true =>
        val newConn = new NettyConnection(config)
        if(connRef.compareAndSet(curr,  newConn)) {
          newConn.connect()
          newConn
        } else connRef.get()
      case false =>
        curr
    }
  }
}
