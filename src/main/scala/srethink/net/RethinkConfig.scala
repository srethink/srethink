package srethink.net

import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext

trait RethinkConfig {
  val hostname: String
  val port: Int
  val magic: Int
  val authenticationKey: String
  val executionContext: ExecutionContext
}

case class NettyRethinkConfig(
  hostname: String,
  port: Int,
  magic: Int,
  authenticationKey: String,
  executionContext: ExecutionContext,
  bossExecutor: Executor,
  workerExecutor: Executor) extends RethinkConfig
