package srethink.net

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import scala.concurrent.ExecutionContext
import srethink.protocol._

trait RethinkConfig {
  val hostname: String
  val authenticationKey: String
  val executionContext: ExecutionContext
  val magic: Int
  val poolSize: Int
}

object RethinkConfig {
  def nettyConfig(
    hostname: String = "127.0.0.1",
    port: Int = 28015,
    authenticationKey: String = "",
    bossExecutor: Executor = Executors.newCachedThreadPool(),
    workerExecutor: Executor = Executors.newCachedThreadPool()
  ) = {
    NettyRethinkConfig(
      hostname = hostname,
      port = port,
      magic = Protocol.V0_3_VALUE,
      poolSize = 1,
      authenticationKey = authenticationKey,
      executionContext = ExecutionContext.fromExecutor(workerExecutor),
      channelFactory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor)
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
  channelFactory: ChannelFactory
) extends RethinkConfig
