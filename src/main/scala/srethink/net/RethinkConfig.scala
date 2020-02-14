package srethink.net

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.util._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import srethink.protocol._
import cats.effect._

trait RethinkConfig {
  val hostname: String
  val authenticationKey: String
  val executionContext: ExecutionContext
  val magic: Int
  val poolSize: Int
  val requestTimeout: FiniteDuration
  val connectTimeout: FiniteDuration
  val cs: ContextShift[IO]
}

object RethinkConfig {
  def nettyConfig(
    hostname: String = "127.0.0.1",
    port: Int = 28015,
    authenticationKey: String = "",
    bossExecutor: Executor = Executors.newCachedThreadPool(),
    workerExecutor: Executor = Executors.newCachedThreadPool(),
    requestTimeout: FiniteDuration = 1.minutes,
    connectTimeout: FiniteDuration = 3.seconds,
    timer: RethinkTimer = DefaultRethinkTimer
  )(implicit cs: ContextShift[IO]) = {
    NettyRethinkConfig(
      hostname = hostname,
      port = port,
      magic = Protocol.V0_3_VALUE,
      poolSize = 1,
      authenticationKey = authenticationKey,
      executionContext = ExecutionContext.fromExecutor(workerExecutor),
      channelFactory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor),
      requestTimeout = requestTimeout,
      connectTimeout = connectTimeout,
      timer = timer
    )
  }
}

case class NettyRethinkConfig(
  hostname: String,
  port: Int,
  magic: Int ,
  poolSize: Int,
  authenticationKey: String,
  executionContext: ExecutionContext,
  channelFactory: ChannelFactory,
  requestTimeout: FiniteDuration,
  connectTimeout: FiniteDuration,
  timer: RethinkTimer
)(implicit val cs: ContextShift[IO]) extends RethinkConfig
