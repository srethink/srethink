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

  override def channelActive(ctx: ChannelHandlerContext) {
    logger.info("Channel active, send handshake...")
    val h = Handshake(config.magic, config.authKey, config.protocol)
    ctx.channel.writeAndFlush(h)
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef) = {
    msg match {
      case "SUCCESS" =>
        logger.info("Handshake finished")
        handshake.tryComplete(Success(true))
      case err: String =>
        logger.info("handshake failure")
        handshake.tryFailure(new Exception(err))
      case m: Message =>
        if(logger.isDebugEnabled) {
          logger.debug(s"Receiving message $msg")
        }
        registry.get(m.token).foreach(_.trySuccess(m))
        registry.remove(m.token)
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    ctx.close()
    makeFail(cause)
  }

  private def makeFail(cause: Throwable) = {
    val removed = for {
      (k, v) <- registry if !v.isCompleted
    } v.tryFailure(cause)
  }
}


class ConnectionInitializer(context: HandlerContext) extends  ChannelInitializer[SocketChannel] {
  override def initChannel(ch: SocketChannel) {
    val pipe = ch.pipeline()
    pipe.addLast(new NettyConnectionCodec(context))
    pipe.addLast(new ConnectionHandler(context))

  }
}
