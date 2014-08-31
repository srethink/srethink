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

trait TestCodec extends RethinkSpec {
  implicit val bookCodec: JsEncoder[Book] with JsDecoder[Book]
  implicit val primaryKeyEncoder: JsEncoder[Either[String, Double]]
  implicit val booksDecoder: JsDecoder[Seq[Book]]
}
trait WithData extends RethinkSpec with RQL with BeforeExample with TestCodec {

  lazy val books = r.table("library", "book")


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
            "$rql_type$" -> "TIME",
            "epoch_time" -> jsNumber((new java.util.Date).getTime / 1000.00)
          )
        )
      )
    )
  }

  def before() = {
    import scala.concurrent.{Await, duration}
    val fut = for {
      _ <- r.dbCreate("library").recover{case e => }
      _ <- r.tableCreate("library", "book").recover{case e => }
      _ <- books.delete()
    } yield true
    Await.ready(fut, duration.Duration.Inf)
  }

}

trait PlayRethinkSpec extends WithData with PlayJsonDef with PlayRethinkFormats {
  import scala.collection.generic._
  implicit lazy val executor = new NettyQueryExecutor(RethinkConfig.nettyConfig())
  implicit lazy val bookCodec = play.api.libs.json.Json.format[Book]
  implicit lazy val primaryKeyEncoder = eitherWrites[String, Double]
  implicit lazy val booksDecoder = play.api.libs.json.Reads.traversableReads[Seq, Book](
    implicitly[CanBuildFrom[Seq[_], Book, Seq[Book]]], bookCodec
  )
}
