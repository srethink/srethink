package srethink.net

import scala.collection.concurrent.TrieMap
import scala.concurrent.{
  Await,
  Promise,
  Future,
  duration
}
import scala.util.Failure
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.buffer._
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.frame._
import org.jboss.netty.handler.codec.string.StringDecoder
import org.slf4j.LoggerFactory

import srethink.protocol.Protocol

class NettyConnection(val config: NettyRethinkConfig) extends Connection {

  val logger = LoggerFactory.getLogger(classOf[Connection])

  val charset = java.nio.charset.Charset.defaultCharset()
  private implicit val executionContext = config.executionContext

  @volatile
  private var channel = None: Option[Channel]
  private val responseMap = TrieMap[Long, Promise[Response]]()
  private val handshake = Promise[String]()

  def close() = channel.foreach { c =>
    c.close()
    c.getCloseFuture().awaitUninterruptibly()
  }

  def isConnected = channel.map(_.isOpen).getOrElse(false)

  def query(query: Query): Future[Response] = {
    logger.debug("sending query with  {}", query)
    val newPromise = Promise[Response]()
    def p = responseMap.putIfAbsent(query.token, newPromise).getOrElse {
      channel.get.write(query)
      newPromise
    }
    for {
      shake <- handshake.future
      resp <- p.future
    } yield resp
  }

  def connect() = {
    logger.info("connecting to host {}", config.hostname)
    val channelFuture = bootstrap().connect(new java.net.InetSocketAddress(config.hostname, config.port))
    channel = Some(channelFuture.getChannel)
    Await.ready(handshake.future, duration.Duration.Inf)
  }

  private def bootstrap() = {
    val bootstrap = new ClientBootstrap(config.channelFactory)
    val bufferFactory = new HeapChannelBufferFactory(java.nio.ByteOrder.LITTLE_ENDIAN)
    bootstrap.setOption("bufferFactory", bufferFactory)
    bootstrap.setOption("child.keepAlive", true)

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

    override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) = {
      e.getCause() match {
        case e: java.net.ConnectException =>
          handshake.complete(new Failure(e))
        case _ => throw e.getCause()
      }
    }

    override def channelConnected(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
      prepareHandshake(ctx)
      sendHandshake(e.getChannel)
    }

    override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
      e.getMessage match {
        case "SUCCESS" =>
          prepareQuery(ctx)
          handshake.success("SUCCESS")
          logger.info("connected successfully")
        case handshakeResult: String =>
          handshake.failure(new ConnectionError(handshakeResult.toString))
        case response: Response =>
          responseMap(response.token).success(response)
          responseMap.remove(response.token)
          logger.debug("response return for {}", response)
      }
    }

    private def prepareQuery(ctx: ChannelHandlerContext) {
      val pipeline = ctx.getPipeline()
      pipeline.replace(frameDecoderName, frameDecoderName, new LengthFieldBasedFrameDecoder(8 * 1024 * 1024, 8, 4))
      pipeline.replace(messageDecoderName, messageDecoderName, new ResponseDecoder(charset))
      pipeline.addFirst(messageEncoderName, new QueryEncoder(charset))
    }

    private def prepareHandshake(ctx: ChannelHandlerContext) {
      val pipeline = ctx.getPipeline()
      pipeline.addFirst(messageDecoderName, new StringDecoder)
      pipeline.addFirst(frameDecoderName, new DelimiterBasedFrameDecoder(1024, Delimiters.nulDelimiter: _*))
    }

    private def sendHandshake(channel: Channel) {
      import config._
      val authKey = authenticationKey.getBytes("ascii")
      //4 bytes magic, 4 bytes key length, 4 bytes protocol
      val bufSize = 4 + 4 + authKey.length + 4
      val buf = ChannelBuffers.buffer(ChannelBuffers.LITTLE_ENDIAN, bufSize)
      buf.writeInt(magic)
      buf.writeInt(authKey.length)
      buf.writeBytes(authKey)
      buf.writeInt(Protocol.JSON_VALUE)
      channel.write(buf)
    }
  }
}
