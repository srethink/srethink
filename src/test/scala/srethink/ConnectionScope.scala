package srethink

import org.specs2.specification._
import srethink.net._
import srethink.protocol._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.Executors

trait TokenGenerator {
  val tokenGen = new java.util.concurrent.atomic.AtomicLong
  def nextToken = tokenGen.incrementAndGet()
}

trait Connected {
  this: TokenGenerator =>
  val cfg = RethinkConfig.nettyConfig(
    version = RethinkConfig.V2
  )
  lazy val connection = new NettyConnection(cfg)
  def connect() = connection.connect()
  def disconnect() = connection.close()

}

trait WithConnection extends Connected with TokenGenerator with Scope {
  connect()
}
