package srethink.net

import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.buffer._
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.handler.codec.frame._
import org.jboss.netty.handler.codec.protobuf._
import org.jboss.netty.handler.codec.string._
import scala.concurrent.{Future, promise, Promise}
import scala.collection.concurrent.TrieMap
import srethink.protocol._

class NettyConnection(val config: NettyRethinkConfig) extends Connection {

  private implicit val executionContext = config.executionContext
  private val responseMap = TrieMap[Long, Promise[Response]]()
  private val handshake = promise[String]
  @volatile
  private var channel = None: Option[Channel]
  @volatile
  private var connected = false

  def close() = channel.foreach(_.close())

  def isConnected = connected

  def query(query: Query): Future[Response] = {
    val newPromise = promise[Response]
    val p = responseMap.putIfAbsent(query.token.get, newPromise).getOrElse {
      channel.get.write(query)
      newPromise
    }

    if(connected) p.future else {
      for {
        shake <- handshake.future
        resp <- p.future
      } yield resp
    }
  }

  def connect() = {
    val channelFuture = bootstrap().connect(new java.net.InetSocketAddress(config.hostname, config.port))
    channel = Some(channelFuture.getChannel)
    channelFuture.sync()
  }

  private def bootstrap() = {
    val channelFactory = new NioClientSocketChannelFactory(
      config.bossExecutor, config.workerExecutor)
    //Set buffer factory
    val bootstrap = new ClientBootstrap(channelFactory)
    val bufferFactory = new HeapChannelBufferFactory(java.nio.ByteOrder.LITTLE_ENDIAN)
    bootstrap.setOption("bufferFactory", bufferFactory)

    //Set pipeline factory
    val pipelineFactory = new ChannelPipelineFactory {
      def getPipeline() = {
        val p = Channels.pipeline()
        p.addLast("handler", new RethinkHandler)
        p
      }
    }
    bootstrap.setPipelineFactory(pipelineFactory)
    bootstrap
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
          connected = true
          handshake.success("SUCCESS")
        case handshakeResult: String =>
          handshake.failure(new ConnectionError(handshakeResult.toString))
        case response: Response =>
          responseMap(response.token.get).success(response)
      }
    }

    private def prepareQuery(ctx: ChannelHandlerContext) {
      val pipeline = ctx.getPipeline()
      pipeline.replace(frameDecoderName, frameDecoderName, new LengthFieldBasedFrameDecoder(1000 * 1024, 0 , 4, 0, 4))
      pipeline.replace(messageDecoderName, messageDecoderName, new ProtobufDecoder(Response.getDefaultInstance()))
      pipeline.addFirst(messageEncoderName, new ProtobufEncoder());
      pipeline.addFirst(frameEncoderName, new LengthFieldPrepender(4));
    }

    private def prepareHandshake(ctx: ChannelHandlerContext) {
      val pipeline = ctx.getPipeline()
      pipeline.addFirst(messageDecoderName, new StringDecoder)
      pipeline.addFirst(frameDecoderName, new DelimiterBasedFrameDecoder(1024, Delimiters.nulDelimiter: _*))
    }

    private def sendHandshake(channel: Channel) {
      import config._
      val authKey = authenticationKey.getBytes("ascii")
      val bufSize = 4 + 4 + authKey.length
      val buf = ChannelBuffers.buffer(ChannelBuffers.LITTLE_ENDIAN, bufSize)
      buf.writeInt(magic)
      buf.writeInt(authKey.length)
      buf.writeBytes(authKey)
      channel.write(buf)
    }
  }
}
