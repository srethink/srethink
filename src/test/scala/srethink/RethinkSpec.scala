package srethink

import cats.effect._
import scala.concurrent.{Future, ExecutionContext, Promise}
import play.api.libs.json.{Format, Json, Writes}
import srethink.net._
import play.api.rql._
import scala.concurrent.ExecutionContext.Implicits._

object Executors {
  val executor = new NettyQueryExecutor(RethinkConfig.nettyConfig())
}

trait RethinkSpec extends munit.FunSuite {
  implicit val executor: QueryExecutor = Executors.executor
}

trait RethinkOperatorSpec extends  WithData {
  def testOp[T: Format](name: String)(f: Var => Expr)(fr: Book => T) = {
    test("perform $name") {
      val b = book(1)
       for {
        ir <- books.insert(Seq(b)).runAs[InsertResult]
        qs <- books.getAll(ir.generated_keys.get).map(f).runAs[Seq[T]]
       } yield {
         assertEquals(qs, Seq(fr(b)))
      }
    }
  }
}


trait WithData extends RethinkSpec {

  def testQuery[T: Format](seq: Book*)(q :Ast)(expectF: T => Boolean)= {
    for {
      ir <- books.insert(seq).runAs[InsertResult]
      r <- q.runAs[T]
    } yield {
      println(stringify(encode[T](r)))
      assert(expectF(r))
    }
  }

  lazy val books = r.db("library").table("book")
  implicit val coAuthorCodec: Format[CoAuthor] = Json.format[CoAuthor]
  implicit val bookCodec: Format[Book] = Json.format[Book]

  def book(i: Int) = {
    Book(
      seq = i,
      title = "title" + i,
      author = "author" + i,
      price = i.toDouble,
      quantity = i,
      releaseDate = new java.util.Date()
    )
  }

  def jsBook(i: Int) = {
    jsObject(
      Seq(
        "seq" -> jsNumber(i),
        "title" -> jsString("title" + i),
        "author" -> jsString("author" + i),
        "price" -> jsNumber(i.toDouble),
        "quantity" -> jsNumber(i),
        "releaseDate" -> jsObject(
          Seq(
            "$reql_type$" -> jsString("TIME"),
            "epoch_time" -> jsNumber((new java.util.Date).getTime / 1000.00),
            "timezone" -> jsString("+08:00")
          )
        )
      )
    )
  }

  override def munitFixtures = List(dataInited)

  val dataInited =
    new munit.FutureFixture[Unit]("ddatabase") {
      def apply(): Unit = ()
      override def beforeEach(ctx: munit.BeforeEach): Future[Unit] = {
        for {
          _ <- r.dbCreate("library").runAs[CreateResult].recover{case e => }
          _ <- r.db("library").tableCreate("book").runAs[CreateResult].recover{case e => }
          _ <- r.db("library").table("book").indexCreate("sequence")(_.seq).runAs[CreateResult].recover { case e => }
          _ <- books.delete().runAs[DropResult]
        } yield ()
      }
    }
}
