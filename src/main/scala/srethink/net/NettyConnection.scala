package srethink.net

import scala.collection.concurrent.TrieMap
import scala.concurrent.{Await, Promise, Future, duration}
import scala.concurrent.duration._
import scala.util._
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.buffer._
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.frame._
import org.jboss.netty.handler.codec.string.StringDecoder
import org.jboss.netty.util._
import org.slf4j.LoggerFactory
import srethink.protocol.Protocol


class RethinkTimer(val interval: FiniteDuration) {
  val timer = new HashedWheelTimer(interval.toMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
  def newSchedule(delay: FiniteDuration)(f: () => Unit) = {
    timer.newTimeout(new TimerTask {
      def run(timeout: Timeout) {
        f()
      }
    }, delay.toMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
  }

  def stop() = timer.stop()
}

object DefaultRethinkTimer extends RethinkTimer(1.seconds)

class NettyConnection(val config: NettyRethinkConfig) extends Connection {

  private val logger = LoggerFactory.getLogger(classOf[Connection])
  private val charset = java.nio.charset.Charset.defaultCharset()
  private implicit val executionContext = config.executionContext
  @volatile
  private var channel = None: Option[Channel]
  private val responseMap = TrieMap[Long, Promise[Response]]()
  private val handshake = Promise[String]()
  private val timer = config.timer

  def close() = channel.foreach { c =>
    c.close()
    c.getCloseFuture().awaitUninterruptibly()
    markAllFail(new RethinkException("Connection already closed"))
  }

  def isConnected = channel.map(_.isOpen).getOrElse(false)

  def query(query: Query): Future[Response] = {
    logger.debug("sending query {}", query)
    val newPromise = Promise[Response]()
    def p = responseMap.putIfAbsent(query.token, newPromise).getOrElse {
      channel.get.write(query).addListener(new ChannelFutureListener {
        def operationComplete(f: ChannelFuture) {
          if(!f.isSuccess()) {
            newPromise.tryComplete(Failure(f.getCause()))
          }
        }
      })
      newPromise
    }
    val timeout = timer.newSchedule(config.requestTimeout) { () =>
      if(!p.isCompleted) {
        p.tryComplete(Failure(new java.util.concurrent.TimeoutException()))
      }
    }
    for {
      shake <- handshake.future
      resp <- p.future.andThen{ case _ => timeout.cancel() }
    } yield resp
  }

  def connect() = {
    logger.info("connecting to host {}", config.hostname)
    val channelFuture = bootstrap().connect(new java.net.InetSocketAddress(config.hostname, config.port))
    channel = Some(channelFuture.getChannel)
    Await.ready(handshake.future, 3.seconds)
  }

  private def markAllFail(ex: Throwable) {
    logger.error("Error caught, set all futures to fail", ex)
    val keys = for( (k, v) <- responseMap if(!v.isCompleted)) yield {
      v.tryComplete(Failure(ex))
      k
    }
    responseMap --= (keys)
  }

  private def bootstrap() = {
    val bootstrap = new ClientBootstrap(config.channelFactory)
    val bufferFactory = new HeapChannelBufferFactory(java.nio.ByteOrder.LITTLE_ENDIAN)
    bootstrap.setOption("bufferFactory", bufferFactory)
    bootstrap.setOption("child.keepAlive", true)
    bootstrap.setOption("connectTimeoutMillis",config.connectTimeout.toMillis)
    //Set pipeline factory
    val pipelineFactory = new ChannelPipelineFactory {
      def getPipeline() = {
        Channels.pipeline(new RethinkHandler)
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
      e.getCause().printStackTrace()
      e.getCause() match {
        case e: java.net.ConnectException =>
          handshake.tryComplete(new Failure(e))
        case ex =>
          handshake.tryComplete(Failure(ex))
          markAllFail(ex)
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
          responseMap.get(response.token).foreach(_.tryComplete(Success(response)))
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
