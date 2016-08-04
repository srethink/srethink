package org.srethink.net

import io.netty.bootstrap._
import io.netty.channel._
import io.netty.channel.nio._
import io.netty.channel.socket.nio._
import java.net._
import java.nio.charset.Charset
import org.srethink.net.ChannelFutures._
import org.slf4j._
import scala.collection.concurrent.TrieMap
import scala.concurrent.{Future, Promise}

case class NettyConnectionConfig(
  host: String = "127.0.0.1",
  port: Int = 28015,
  magic: Int = Version.V0_4,
  protocol: Int = Protocol.JSON,
  authKey: String = "",
  connectTimeoutMills: Int = 3000,
  readTimeoutMillis: Int = 10000,
  eventLoopGroup: EventLoopGroup = new NioEventLoopGroup(),
  channelClass: Class[_ <: Channel] = classOf[NioSocketChannel],
  charset: Charset = Charset.defaultCharset())


class NettyConnection(val config: NettyConnectionConfig) extends Connection {

  private val handshake = Promise[Boolean]
  private val registry = new TrieMap[Long, Promise[Message]]
  private val logger = LoggerFactory.getLogger(classOf[NettyConnection])
  private val context = HandlerContext(config, registry, handshake, logger)
  private implicit val ec = org.srethink.exec.trampoline
  def closed = channel.map(!_.isOpen)

  def execute(m: Message): Future[Message] = {
    if(logger.isDebugEnabled) {
      logger.debug(s"[NettyConnection-execute] Sending message ${m.body}")
    }
    channel.flatMap { c =>
      val p = Promise[Message]
      registry.put(m.token, p)
      c.writeAndFlush(m).addListener(new ChannelFutureListener {
        def operationComplete(f: ChannelFuture) = {
          if(!f.isSuccess()) {
            p.tryFailure(f.cause())
          }
        }
      })
      p.future
    }
  }

  lazy val connectFuture = {
    val address = new InetSocketAddress(config.host, config.port)
    bootstrap().connect(address)
  }

  def connect() = {
    logger.info(s"Open connection to ${config.host}:${config.port}")
    connectFuture
    connectFuture.awaitUninterruptibly()
  }

  def close() = {
    connectFuture.channel.close().awaitUninterruptibly
  }

  lazy val channel = {
    for {
      cf <- connectFuture.asScala
      _ <- handshake.future
    } yield cf.channel
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
      val p = Promise[ChannelFuture]
      f.addListener(new  ChannelFutureListener {
        override def operationComplete(f: ChannelFuture) = {
          if(f.isSuccess()) {
            p.success(f)
          } else {
            p.failure(f.cause())
          }
        }
      })
      p.future
    }
  }
}
