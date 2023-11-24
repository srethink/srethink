package org.srethink.net

import cats.syntax.all._
import cats.effect._
import io.netty.bootstrap._
import io.netty.channel._
import io.netty.channel.nio._
import io.netty.channel.socket.nio._
import java.net._
import java.nio.charset.Charset
import org.slf4j._
import scala.concurrent.duration._


case class NettyConnectionConfig(
  host: String = "127.0.0.1",
  port: Int = 28015,
  magic: Int = Version.V0_4,
  protocol: Int = Protocol.JSON,
  authKey: String = "",
  connectTimeoutMills: Int = 3000,
  readTimeoutMillis: Int = 30000,
  eventLoopGroup: EventLoopGroup = new NioEventLoopGroup(),
  channelClass: Class[_ <: Channel] = classOf[NioSocketChannel],
  charset: Charset = Charset.defaultCharset())


class NettyConnection[F[_]](
  context: HandlerContext[F]
)(implicit F: Async[F]) extends Connection[F] {


  private val logger = LoggerFactory.getLogger(classOf[NettyConnection[F]])
  private val channel = context.handshake.get *> context.channel.get.rethrow

  def closed = channel.map(_.isActive)

  def execute(m: Message): F[Message] = {
    if(logger.isDebugEnabled) {
      logger.debug(s"[NettyConnection-execute] Sending message ${m.body}")
    }

    def failOnTimeout(): F[Throwable] = {
      F.sleep(context.config.readTimeoutMillis.millis).as(new Exception(s"query timeout after ${context.config.readTimeoutMillis} millis}")).map { e =>
        println("timeout exceed")
        e
      }
    }

    val res = for {
      c <- channel
      _ <- F.delay(c.writeAndFlush(m))
      d <- Deferred[F, Message]
      _ <- F.delay(context.registry.put(m.token, d))
      t <- F.start(failOnTimeout())
      r <- F.race(t.join, d.get.flatTap(_ => t.cancel))
    } yield r
    res.flatTap { _ =>
      F.delay(context.registry.remove(m.token))
    }.rethrow
  }

  def close() = {
    channel.map { c =>
      c.close()
    }
  }
}

object NettyConnection {

  private def bootstrap[F[_]](context: HandlerContext[F])(implicit F: Async[F]) = {
    new Bootstrap()
      .option[Integer](ChannelOption.CONNECT_TIMEOUT_MILLIS, context.config.connectTimeoutMills)
      .option[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, true)
      .group(context.config.eventLoopGroup)
      .channel(context.config.channelClass)
      .handler(new ConnectionInitializer(context))
  }

  def connect[F[_]](context: HandlerContext[F])(implicit F: Async[F]) =  {
    val remoteAddr = new InetSocketAddress(context.config.host, context.config.port)
    bootstrap(context).connect(remoteAddr).addListener(new ChannelFutureListener {
      override def operationComplete(f: ChannelFuture) = {
        val thunk = if(f.isSuccess) {
          context.channel.complete(Right(f.channel()))
        } else {
          context.channel.complete(Left(f.cause()))
        }
        F.toIO(thunk).unsafeRunSync()
      }
    })
    new NettyConnection[F](context)
  }

  def create[F[_]](config: NettyConnectionConfig)(implicit F: Async[F]) = {
    (Deferred[F, Either[Throwable, Channel]], Deferred[F, Either[Throwable, Boolean]]).mapN {
      case (ch, h) =>
        connect(HandlerContext(
          config = config,
          registry = new java.util.concurrent.ConcurrentHashMap[Long, Deferred[F, Message]],
          handshake = h,
          channel = ch
        ))
    }
  }

}
