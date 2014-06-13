package srethink

import org.specs2.specification._
import srethink.net._
import srethink.protocol._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.Executors

trait WithConnection extends Scope {

  val tokenGen = new java.util.concurrent.atomic.AtomicLong

  val cfg = NettyRethinkConfig(
    hostname = "localhost",
    port = 28015,
    magic = VersionDummy.Version.V0_2_VALUE,
    authenticationKey = "",
    bossExecutor = Executors.newCachedThreadPool(),
    workerExecutor = Executors.newCachedThreadPool(),
    executionContext = global
  )

  def nextToken = tokenGen.incrementAndGet()

  val connection = new NettyConnection(cfg)
  connection.connect()
}
