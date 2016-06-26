package org.srethink.net

import io.netty.bootstrap._
import io.netty.channel._
import io.netty.channel.nio._
import io.netty.channel.socket.nio._
import java.net._
import java.nio.charset.Charset
import java.util.concurrent.atomic._
import org.srethink.net.ChannelFutures._
import org.slf4j._
import scala.collection.concurrent.TrieMap
import scala.concurrent.{Future, Promise, ExecutionContext}
import scala.util._

case class NettyConnectionConfig(
  host: String = "127.0.0.1",
  port: Int = 28015,
  magic: Int = Version.V0_4,
  protocol: Int = Protocol.JSON,
  authKey: String = "",
  connectTimeoutMills: Int = 3000,

  eventLoopGroup: EventLoopGroup = new NioEventLoopGroup(),
  channelClass: Class[_ <: Channel] = classOf[NioSocketChannel],
  charset: Charset = Charset.defaultCharset())


class NettyConnection(val config: NettyConnectionConfig) extends Connection {

  private val handshake = Promise[Boolean]
  private val registry = new TrieMap[Long, Promise[Message]]
  private val logger = LoggerFactory.getLogger(classOf[NettyConnection])
  private val context = HandlerContext(config, registry, handshake, logger)
  private implicit val ec = org.srethink.exec.trampoline
  @volatile
  var _closed = false

  def closed = _closed

  def execute(m: Message): Future[Message] = {
    logger.debug("Sending query {}", m)
    channel.flatMap { c =>
      registry.put(m.token, Promise[Message])
      c.writeAndFlush(m)
      registry(m.token).future
    }
  }

  def connect() = {
    channel.map { ch =>
      ch.closeFuture.addListener(new ChannelFutureListener {
        def operationComplete(f: ChannelFuture) = {
          _closed = true
        }
      })
    }.onFailure {
      case ex: Throwable => _closed = true
    }
    channel.flatMap(_ => handshake.future).map(_ => {})
  }

  def close() = {
    channel.flatMap(_.close().asScala).map(_ => {})
  }

  lazy val channel = {
    val address = new InetSocketAddress(config.host, config.port)
    bootstrap().connect(address).asScala
  }

  private def bootstrap() = {
    new Bootstrap()
      .option[Integer](ChannelOption.CONNECT_TIMEOUT_MILLIS, config.connectTimeoutMills)
      .option[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, true)
      .group(config.eventLoopGroup)
      .channel(config.channelClass)
      .handler(new ConnectionInitializer(context))
  }
}

object ChannelFutures {
  implicit class ChannelFutureSyntax(val f: ChannelFuture) extends AnyVal {
    def asScala = {
      val p = Promise[Channel]
      f.addListener(new  ChannelFutureListener {
        override def operationComplete(f: ChannelFuture) = {
          if(f.isSuccess()) {
            p.success(f.channel())
          } else {
            p.failure(f.cause())
          }
        }
      })
      p.future
    }
  }
}
