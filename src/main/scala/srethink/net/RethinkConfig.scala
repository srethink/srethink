package srethink.net

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import scala.concurrent.ExecutionContext
import srethink.protocol._

trait RethinkConfig {
  val hostname: String
  val port: Int
  val magic: Int
  val authenticationKey: String
  val executionContext: ExecutionContext
}

object RethinkConfig {
  def nettyConfig(
    hostname: String = "127.0.0.1",
    port: Int = 28015,
    magic: Int = VersionDummy.Version.V0_3_VALUE,
    protocol: Option[Int] = Some(VersionDummy.Protocol.PROTOBUF_VALUE),
    authenticationKey: String = "",
    bossExecutor: Executor = Executors.newCachedThreadPool(),
    workerExecutor: Executor = Executors.newCachedThreadPool()
  ) = {
    NettyRethinkConfig(
      hostname = hostname,
      port = port,
      magic = magic,
      protocol = protocol,
      authenticationKey = authenticationKey,
      executionContext = ExecutionContext.fromExecutor(workerExecutor),
      channelFactory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor)
    )
  }
}

case class NettyRethinkConfig(
  hostname: String,
  port: Int,
  magic: Int,
  protocol: Option[Int],
  authenticationKey: String,
  executionContext: ExecutionContext,
  channelFactory: ChannelFactory
) extends RethinkConfig
