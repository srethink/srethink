package org.srethink.net

import io.netty.buffer._
import io.netty.channel._
import io.netty.handler.codec._
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

class NettyConnectionCodec(context: HandlerContext) extends ByteToMessageCodec[Any] {

  type Ctx = ChannelHandlerContext
  type Buf = ByteBuf
  type Outs = java.util.List[AnyRef]

  private val charset = context.config.charset
  private val handshakeComplete = new AtomicBoolean(false)

  override def decode(ctx: Ctx, buf: Buf, outs: Outs) = {
    if(handshakeComplete.get()) {
      decodeMessage(ctx, buf, outs)
    } else {
      decodeHandshake(ctx, buf, outs)
    }
  }

  override def encode(ctx: Ctx, msg: Any, buf: Buf) = {
    msg match {
      case m: Message =>
        encodeMessage(m, buf)
      case h: Handshake =>
        encodeHandshake(h, buf)
    }
  }

  private def decodeHandshake(ctx: Ctx, buf: Buf, outs: Outs) = {
    val out = HandshakeFrameDecoder.decodeHandshake(ctx, buf)
    if(out != null) {
      outs.add(out)
      handshakeComplete.set(true)
    }
  }

  private def decodeMessage(ctx: Ctx, buf: Buf, outs: Outs) = {
   val out = MessageFrameDecoder.decodeMessage(ctx, buf.order(ByteOrder.LITTLE_ENDIAN))
    if(out != null) {
      outs.add(out)
    }
  }

  private def encodeMessage(m: Message, buf: Buf) = {
    val bodyBytes = m.body.getBytes(charset)
    val out = buf.order(ByteOrder.LITTLE_ENDIAN)
    out.writeLong(m.token)
    out.writeInt(bodyBytes.length)
    out.writeBytes(bodyBytes)
  }

  private def encodeHandshake(m: Handshake, buf: Buf) = {
    val out = buf.order(ByteOrder.LITTLE_ENDIAN)
    val keyBytes = m.authKey.getBytes("ascii")
    out.capacity(8 + 4 + keyBytes.length)
      .writeInt(m.magic)
      .writeInt(keyBytes.length)
      .writeBytes(keyBytes)
      .writeInt(m.protocol)
  }

  private[net] object MessageFrameDecoder
      extends LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 8 * 1024 * 1024, 8, 4, 0, 0, true) {
    def decodeMessage(ctx: Ctx, buf: Buf) = {
      val frame = decode(ctx, buf)
      frame match {
        case m : ByteBuf =>
          val token = m.readLong()
          val length = m.readInt()
          val body = m.toString(12, length, charset)
          Message(token, body)
        case _ => frame
      }
    }
  }
  private[net] object HandshakeFrameDecoder
      extends DelimiterBasedFrameDecoder(1024, Delimiters.nulDelimiter: _*) {
    def decodeHandshake(ctx: Ctx, buf: Buf) = {
      val frame = decode(ctx, buf)
      frame match {
        case m:ByteBuf =>
          val bytes = new Array[Byte](m.readableBytes())
          m.readBytes(bytes)
          new String(bytes)
        case _ => frame
      }
    }
  }
}
