package org.srethink.net

import io.netty.bootstrap._
import io.netty.channel._
import java.util.concurrent.atomic._
import org.srethink.core._
import org.srethink.net.ChannelFutures._
import scala.concurrent.{Future, Promise, ExecutionContext}
import scala.collection.concurrent.TrieMap
import scala.util._

case class NettyConnectionConfig(
  host: String,
  port: Int,
  magic: Int,
  protocol: Int,
  authKey: String,
  connectTimeoutMills: Int,
  eventLoopGroup: EventLoopGroup,
  channelClass: Class[Channel],
  executionContext: ExecutionContext,
  charset: java.nio.charset.Charset
)

class NettyConnection(val config: NettyConnectionConfig) extends Connection {

  private val registry = TrieMap[Long, Promise[Message]]()
  private val handshake = Promise[Boolean]
  private implicit val ec = config.executionContext

  def channel = {
    val nettyFut = bootstrap().connect()
    for {
      c <- asScala(nettyFut)
      _ <- handshake.future
    } yield c
  }

  def execute(m: Message): Future[Message] = {
    channel.flatMap { c =>
      registry.put(m.token, Promise[Message])
      c.writeAndFlush(m)
      registry(m.token).future
    }
  }

  private def bootstrap() = {
    new Bootstrap()
      .option[Integer](ChannelOption.CONNECT_TIMEOUT_MILLIS, config.connectTimeoutMills)
      .group(config.eventLoopGroup)
      .channel(config.channelClass)
      .handler(new NettyConnectionCodec(config.charset))
      .handler(Handler)
  }

  @ChannelHandler.Sharable
  object Handler extends ChannelHandlerAdapter {

    def channelActive(ctx: ChannelHandlerContext) {
      val handshake = Handshake(config.magic, config.authKey, config.protocol)
      ctx.writeAndFlush(handshake);
    }

    def channelRead(ctx: ChannelHandlerContext, msg: AnyRef) = {
      msg match {
        case "SUCCESS" => handshake.tryComplete(Success(true))
        case m: Message =>
          registry.get(m.token).foreach(_.trySuccess(m))
          registry.remove(m.token)
        case err: String =>
          handshake.tryFailure(new Exception(err))
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

}

object ChannelFutures {

  def asScala(f: ChannelFuture) = {
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
