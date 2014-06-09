package srethink.net

import scala.concurrent.Future
import srethink.protocol._
import srethink.core._


trait Connection {

  val config: RethinkConfig

  def connect()

  def query(query: Query): Future[Response]

  def close()
}
