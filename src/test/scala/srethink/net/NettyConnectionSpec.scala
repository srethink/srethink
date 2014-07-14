package srethink.net

import org.specs2.mutable.Specification
import org.specs2.specification._
import srethink.protocol._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.immutable._
import srethink._

class NettyConnectionSpec extends Specification {

  "connection" should  {
    "connect to server" in new srethink.WithConnection {
      //wait connection success
      queryExecutor.connectionManager.get.isConnected must beTrue
    }
  }
}
