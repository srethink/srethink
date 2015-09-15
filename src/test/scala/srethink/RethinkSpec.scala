package srethink

import org.specs2.mutable.Specification
import org.specs2.specification._
import play.api.libs.json.{Format, Json, Writes}
import srethink.net._
import play.api.rql._

object Executors {
  val executor = new NettyQueryExecutor(RethinkConfig.nettyConfig())
}

trait RethinkSpec extends Specification {
  sequential
  implicit val executor: QueryExecutor = Executors.executor
}

trait RethinkOperatorSpec extends  WithData {
  def testOp[T: Format](name: String)(f: Var => Expr)(fr: Book => T) = {
    s"perform $name" in {
      val b = book(1)
      val fut = for {
        ir <- books.insert(Seq(b)).runAs[InsertResult]
        qs <- books.getAll(ir.generated_keys.get).map(f).runAs[Seq[T]]
      } yield {
        qs must contain(exactly(fr(b)))
      }
      fut.andThen {
        case scala.util.Failure(e) =>
          println(e.getMessage)
      }.await(10)
    }
  }
}


trait WithData extends RethinkSpec with BeforeExample  {

  def testQuery[T: Format](seq: Book*)(q :Ast)(expectF: T => Boolean)= {
    val fut = for {
      ir <- books.insert(seq).runAs[InsertResult]
      r <- q.runAs[T]
    } yield {
      println(stringify(encode[T](r)))
      expectF(r)
    }
    fut.andThen {
      case scala.util.Failure(e) =>
        e.printStackTrace
    }.await(10)
  }

  lazy val books = r.db("library").table("book")
  implicit val coAuthorCodec: Format[CoAuthor] = Json.format[CoAuthor]
  implicit val bookCodec: Format[Book] = Json.format[Book]

  def book(i: Int) = {
    Book(
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

  def before() = {
    import scala.concurrent.{Await, duration}
    val fut = for {
      _ <- r.dbCreate("library").runAs[CreateResult].recover{case e => }
      _ <- r.db("library").tableCreate("book").runAs[CreateResult].recover{case e => }
      _ <- books.delete().runAs[DropResult]
    } yield true
    Await.ready(fut, duration.Duration.Inf)
  }
}
