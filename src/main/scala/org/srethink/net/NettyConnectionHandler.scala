package org.srethink.net

import io.netty.channel._
import io.netty.channel.socket._
import org.slf4j._
import scala.collection.concurrent.TrieMap
import scala.concurrent._
import scala.util._

private case class HandlerContext(
  config: NettyConnectionConfig,
  registry: TrieMap[Long, Promise[Message]],
  handshake: Promise[Boolean],
  logger: Logger
)

private class ConnectionHandler(context: HandlerContext) extends ChannelInboundHandlerAdapter {
  private val config = context.config
  private val registry = context.registry
  private val handshake = context.handshake
  private val logger = context.logger

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
        handshake.tryComplete(Success(true))
      case err: String =>
        logger.info("handshake failure")
        val ex = new Exception(err)
        handshake.tryFailure(ex)
        makeFail(ex)
        ctx.close()
      case m: Message =>
        if(logger.isDebugEnabled) {
          logger.debug(s"Receiving message $msg")
        }
        registry.get(m.token).foreach(_.trySuccess(m))
        registry.remove(m.token)
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.warn("Uncaught exception, make all pending request fail", cause)
    makeFail(cause)
    ctx.close()
  }
  private def makeFail(cause: Throwable) = {
    for {
      (k, v) <- registry if !v.isCompleted
    } v.tryFailure(cause)
  }
}


class ConnectionInitializer(context: HandlerContext) extends  ChannelInitializer[SocketChannel] {
  val cfg = context.config
  override def initChannel(ch: SocketChannel): Unit = {
    val pipe = ch.pipeline()
    pipe.addLast(new NettyConnectionCodec(context))
    pipe.addLast(new ConnectionHandler(context))

  }
}
