package srethink.net

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import scala.concurrent.ExecutionContext
import srethink.protocol._

trait RethinkConfig {
  val hostname: String
  val version: VersionDummy.Version.EnumVal
  val authenticationKey: String
  val executionContext: ExecutionContext
  def magic = version.id
  def protocol = version match {
    case VersionDummy.Version.V0_3 =>
      Some(VersionDummy.Protocol.PROTOBUF_VALUE)
    case _ => None
  }
}

object RethinkConfig {
  def nettyConfig(
    hostname: String = "127.0.0.1",
    port: Int = 28015,
    version: VersionDummy.Version.EnumVal = VersionDummy.Version.V0_3,
    authenticationKey: String = "",
    bossExecutor: Executor = Executors.newCachedThreadPool(),
    workerExecutor: Executor = Executors.newCachedThreadPool()
  ) = {
    NettyRethinkConfig(
      hostname = hostname,
      port = port,
      version = version,
      authenticationKey = authenticationKey,
      executionContext = ExecutionContext.fromExecutor(workerExecutor),
      channelFactory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor)
    )
  }

  val V1 = VersionDummy.Version.V0_1
  val V2 = VersionDummy.Version.V0_2
  val V3 = VersionDummy.Version.V0_3
}

case class NettyRethinkConfig(
  hostname: String,
  port: Int,
  version: VersionDummy.Version.EnumVal,
  authenticationKey: String,
  executionContext: ExecutionContext,
  channelFactory: ChannelFactory
) extends RethinkConfig
