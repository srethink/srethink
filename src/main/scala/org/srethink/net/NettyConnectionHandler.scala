package org.srethink.net

import cats.effect._
import io.netty.channel._
import io.netty.channel.socket._
import java.util.concurrent.ConcurrentHashMap
import org.slf4j._

private case class HandlerContext[F[_]](
  config: NettyConnectionConfig,
  registry: ConcurrentHashMap[Long, Deferred[F, Message]],
  handshake: Deferred[F, Either[Throwable, Boolean]],
  channel: Deferred[F, Either[Throwable, Channel]]
)

private class ConnectionHandler[F[_]](context: HandlerContext[F])(implicit F: Async[F]) extends ChannelInboundHandlerAdapter {
  private val config = context.config
  private val registry = context.registry
  private val handshake = context.handshake
  private val logger = LoggerFactory.getLogger(this.getClass)

  override def channelActive(ctx: ChannelHandlerContext) = {
    val ch = ctx.channel
    val h = Handshake(config.magic, config.authKey, config.protocol)
    logger.info(s"Channel ${ch} active, send handshake message: ${h}")
    ch.writeAndFlush(h)
  }

  override def channelInactive(ctx: ChannelHandlerContext) = {
    val ch = ctx.channel
    logger.info(s"${ch} become inactive close it")
    ch.close()
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef) = {
    msg match {
      case "SUCCESS" =>
        logger.info(s"Connected to ${config.host}:${config.port}")
        F.toIO(handshake.complete(Right(true))).unsafeRunSync()
      case err: String =>
        F.toIO(handshake.complete(Left(new Exception(s"handshake error $err")))).unsafeRunSync()
        ctx.close()
      case m: Message =>
        if(logger.isDebugEnabled) {
          logger.debug(s"Registry: ${registry}, receive ${m}, complete ${m.token}")
        }
        Option(registry.get(m.token)).foreach { defer =>
          F.toIO(defer.complete(m)).unsafeRunSync()
          println(s"completed ${m.token}")
        }
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.warn("Uncaught exception, make all pending request fail", cause)
    ctx.close()
  }
}


class ConnectionInitializer[F[_]](context: HandlerContext[F])(implicit F: Effect[F]) extends  ChannelInitializer[SocketChannel] {
  val cfg = context.config
  override def initChannel(ch: SocketChannel): Unit = {
    val pipe = ch.pipeline()
    pipe.addLast(new NettyConnectionCodec(context))
    pipe.addLast(new ConnectionHandler(context))

  }
}
