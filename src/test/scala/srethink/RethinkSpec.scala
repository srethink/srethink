package srethink

import org.specs2.mutable.Specification
import org.specs2.specification._
import play.api.rql.{PlayRethinkFormats, PlayJsonDef}
import srethink.ast._
import srethink.net._
import play.api.libs.json._

trait RethinkSpec extends Specification with RQL {
  sequential
  implicit val executor: QueryExecutor
}

trait RethinkOperatorSpec extends RethinkSpec with WithData {
  def testOp(name: String)(f: Var => Expr)(fr: Book => JsValue) = {
    s"perform $name" in {
      val b = book(1)
      val fut = for {
        ir <- books.insert(Seq(b)).runAs[InsertResult]
        qs <- books.getAll(ir.generated_keys.get).map(f).run
      } yield {
        exactJsArray(qs) must contain(exactly(fr(b)))
      }
      fut.await
    }
  }
}

trait TestCodec extends RethinkSpec {
  implicit val bookCodec: JsEncoder[Book] with JsDecoder[Book]
  implicit val primaryKeyEncoder: JsEncoder[Either[String, Double]]
  implicit val booksDecoder: JsDecoder[Seq[Book]]
}
trait WithData extends RethinkSpec with RQL with BeforeExample with TestCodec {

  lazy val books = r.db("library").table("book")

  def exactJsArray(v: JsValue) = {
    unapplyJsArray(v.asInstanceOf[JsArray])
  }

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
            "$reql_type$" -> "TIME",
            "epoch_time" -> jsNumber((new java.util.Date).getTime / 1000.00),
            "timezone" -> "+08:00"
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

trait PlayRethinkSpec extends WithData with PlayJsonDef with PlayRethinkFormats {
  import scala.collection.generic._
  implicit lazy val executor = new NettyQueryExecutor(RethinkConfig.nettyConfig())
  implicit lazy val bookCodec = play.api.libs.json.Json.format[Book]
  implicit lazy val primaryKeyEncoder = eitherWrites[String, Double]
  implicit lazy val booksDecoder = implicitly[Format[Seq[Book]]]
}
