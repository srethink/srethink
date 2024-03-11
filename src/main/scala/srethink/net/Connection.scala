package srethink.net

import scala.concurrent.Future
import srethink.protocol._

trait Connection {

  val config: RethinkConfig

  def connect(): Unit

  def isConnected: Boolean

  def query(query: Query): Future[Response]

  def close(): Unit
}
