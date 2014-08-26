package srethink.net

import org.jboss.netty.handler.codec.oneone._
import java.nio.charset.Charset
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.frame._
import org.jboss.netty.buffer._
import org.slf4j.LoggerFactory

class QueryEncoder(charset: Charset = Charset.defaultCharset()) extends OneToOneEncoder {

  override def encode(ctx: ChannelHandlerContext, channel: Channel, msg: AnyRef) = {
    msg match {
      case query: Query =>
        val queryLength = query.body.getBytes(charset).length
        //8 bytes token, and 4 byte query length
        val headerBuf = channel.getConfig().getBufferFactory().getBuffer(8 + 4)
        headerBuf.writeLong(query.token)
        headerBuf.writeInt(queryLength)
        val bodyBuf = copiedBuffer(headerBuf.order(), query.body, charset)
        wrappedBuffer(headerBuf, bodyBuf)
    }
  }
}

class ResponseDecoder(charset: Charset = Charset.defaultCharset())
    extends OneToOneDecoder {

  override def decode(ctx: ChannelHandlerContext, channel: Channel, msg: AnyRef) = {
    msg match {
      case m : ChannelBuffer =>
        val token = m.readLong()
        val length = m.readInt()
        val body = m.toString(12, length, charset)
        Response(token, body)
      case _ => msg
    }
  }
}
