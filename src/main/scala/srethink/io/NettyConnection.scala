package srethink.io

import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.buffer._
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.handler.codec.frame._
import org.jboss.netty.handler.codec.protobuf._
import org.jboss.netty.handler.codec.string._
import scala.concurrent.{Future, promise, Promise}
import scala.collection.concurrent.TrieMap
import srethink.core._
import srethink.protocol._

class NettyConnection(val config: NettyRethinkConfig) extends Connection {

  private implicit val executionContext = config.executionContext
  private val responseMap = TrieMap[Long, Promise[Response]]()
  private val handshake = promise[String]
  @volatile
  private var channel = None: Option[Channel]

  def close() = channel.foreach(_.close())

  def query(query: Query): Future[Response] = {
    val p = promise[Response]
    val fut = responseMap.putIfAbsent(query.token.get, p).getOrElse(p)
    for {
      shake <- handshake.future
      resp <- p.future
    } yield resp
  }

  def connect() = {
    val channelFuture = bootstrap.connect(new java.net.InetSocketAddress(config.hostname, config.port))
    channel = Some(channelFuture.getChannel)
    channelFuture.sync()
  }

  private def createBootstrap() = {
    val channelFactory = new NioClientSocketChannelFactory(
      config.bossExecutor, config.workerExecutor
    )
    new ClientBootstrap(channelFactory)
  }

  class RethinkHandler extends SimpleChannelUpstreamHandler {

    private val frameDecoderName = "frameDecoder"
    private val messageDecoderName = "messageDecoder"
    private val frameEncoderName = "frameEncoder"
    private val messageEncoderName = "messageEncoder"

    override def channelConnected(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
      prepareHandshake(ctx)
      sendHandshake(e.getChannel)
    }

    override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent)  {
      e.getMessage match {
        case "SUCCESS" =>
          prepareQuery(ctx)
          handshake.success("SUCCESS")
        case handshakeResult: String =>
          handshake.failure(new RethinkError(handshakeResult.toString))
        case response: Response =>
          responseMap(response.token.get).success(response)
      }
    }

    private def prepareQuery(ctx: ChannelHandlerContext) {
      val pipeline = ctx.getPipeline()
      pipeline.replace(frameDecoderName, frameDecoderName, new LengthFieldBasedFrameDecoder(1000 * 1024, 0 , 4, 0, 4))
      pipeline.replace(messageDecoderName, frameDecoderName, new ProtobufDecoder(Response.getDefaultInstance()))
      pipeline.addLast(frameEncoderName, new LengthFieldPrepender(4));
      pipeline.addLast(messageEncoderName, new ProtobufEncoder());
    }

    private def prepareHandshake(ctx: ChannelHandlerContext) {
      val pipeline = ctx.getPipeline()
      pipeline.addLast(frameDecoderName, new DelimiterBasedFrameDecoder(1024, Delimiters.nulDelimiter: _*))
      pipeline.addLast(messageDecoderName, new StringDecoder)
    }

    private def sendHandshake(channel: Channel) {
      import config._
      val authKeySize = authenticationKey.getBytes.length
      val bufSize = 4 + 4 + authKeySize
      val buf = ChannelBuffers.buffer(ChannelBuffers.LITTLE_ENDIAN, bufSize)
      buf.writeInt(magic)
      buf.writeInt(authKeySize)
      buf.writeBytes(authenticationKey.getBytes)
      channel.write(buf)
    }
  }
}
