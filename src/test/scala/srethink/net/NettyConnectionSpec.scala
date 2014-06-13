package srethink.net

import org.specs2.mutable.Specification
import org.specs2.specification._
import srethink.core._
import srethink.protocol._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.immutable._

class NettyConnectionSpec extends Specification {

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

  trait WithConn extends Scope {
    val connection = new NettyConnection(cfg)
    connection.connect()
  }


  def createDb(name: String) = {
    Query(
      `token` = Some(tokenGen.incrementAndGet()),
      `type` = Some(Query.QueryType.START),
      `acceptsRJson` = Some(true),
      `query` = Some(
        Term(
          `type` = Some(Term.TermType.DB_CREATE),
          `args` = Seq(
            Term(
              `type` = Some(Term.TermType.DATUM),
              `datum` = Some(
                Datum(
                  `type` = Some(Datum.DatumType.R_STR),
                  `rStr` = Some(name)
                )
              )
            )
          )
        )
      )
    )
  }

  def dropDb(name: String) = {
    Query(
      `token` = Some(tokenGen.incrementAndGet()),
      `type` = Some(Query.QueryType.START),
      `query` = Some(
        Term(
          `type` = Some(Term.TermType.DB_DROP),
          `args` = Seq(
            Term(
              `type` = Some(Term.TermType.DATUM),
              `datum` = Some(
                Datum(
                  `type` = Some(Datum.DatumType.R_STR),
                  `rStr` = Some(name)
                )
              )
            )
          )
        )
      )
    )
  }

  "connection" should  {
    "connect to server" in new WithConn {
      //wait connection success
      Thread.sleep(1000)
      connection.isConnected must beEqualTo(true)
    }

    "create db" in new WithConn {
      val createFut = connection.query(createDb("test_123"))
      val createR = Await.result(createFut, Duration.Inf)
      createR.`type`.get must beEqualTo(Response.ResponseType.SUCCESS_ATOM)

      val dropFut = connection.query(dropDb("test_123"))
      val dropR = Await.result(dropFut, Duration.Inf)
    }
  }
}
