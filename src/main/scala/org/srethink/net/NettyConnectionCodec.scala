package org.srethink.net

import io.netty.buffer._
import io.netty.channel._
import io.netty.handler.codec._
import java.nio.charset.Charset
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.concurrent.TrieMap
import scala.concurrent._

@ChannelHandler.Sharable
class NettyConnectionCodec(charset: Charset) extends ByteToMessageCodec[Any] {

  type Ctx = ChannelHandlerContext
  type Buf = ByteBuf
  type Outs = java.util.List[AnyRef]

  val handshakeComplete = new AtomicBoolean(false)

  override def decode(ctx: Ctx, buf: Buf, outs: Outs) = {
    if(handshakeComplete.compareAndSet(false, true)) {
      decodeHandshake(ctx, buf, outs)
    } else {
      decodeMessage(ctx, buf, outs)
    }
  }

  override def encode(ctx: Ctx, msg: Any, buf: Buf) = {
    if(handshakeComplete.compareAndSet(false, true)) {
      encodeHandshake(msg.asInstanceOf[Handshake], buf)
    } else {
      encodeMessage(msg.asInstanceOf[Message], buf)
    }
  }

  private def decodeHandshake(ctx: Ctx, buf: Buf, outs: Outs) = {
    val out = HandshakeFrameDecoder.decodeHandshake(ctx, buf)
    if(out != null) {
      outs.add(out)
    }
  }

  private def decodeMessage(ctx: Ctx, buf: Buf, outs: Outs) = {
    val frame = MessageFrameDecoder.decodeMessage(ctx, buf)
    if(frame != null) {
      val frameBuf = frame.asInstanceOf[Buf]
      val token = frameBuf.readLong()
      val length = frameBuf.readInt()
      val body = frameBuf.toString(12, length, charset)
      outs.add(Message(token, body))
    }
  }

  private def encodeMessage(m: Message, buf: Buf) = {
    val bodyBytes = m.body.getBytes(charset)
    buf.order(ByteOrder.LITTLE_ENDIAN)
    buf.writeLong(m.token)
    buf.writeInt(bodyBytes.size)
    buf.writeBytes(bodyBytes)
  }

  private def encodeHandshake(m: Handshake, buf: Buf) = {
    buf.order(ByteOrder.LITTLE_ENDIAN)
    val keyBytes = m.authKey.getBytes("ascii")
    buf.writeInt(m.magic)
    buf.writeInt(keyBytes.size)
    buf.writeBytes(keyBytes)
    buf.writeInt(m.protocol)
  }

  private[net] object MessageFrameDecoder
      extends LengthFieldBasedFrameDecoder(8 * 1024 * 1024, 8, 4) {
    def decodeMessage(ctx: Ctx, buf: Buf) = {
      decode(ctx, buf)
    }
  }
  private[net] object HandshakeFrameDecoder
      extends DelimiterBasedFrameDecoder(1024, Delimiters.nulDelimiter: _*) {
    def decodeHandshake(ctx: Ctx, buf: Buf) = {
      decode(ctx, buf)
    }
  }
}
