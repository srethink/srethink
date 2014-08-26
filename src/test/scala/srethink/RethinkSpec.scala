package srethink

import org.specs2.mutable.Specification
import org.specs2.specification._
import play.api.rql.PlayJsonDef
import srethink.ast._
import srethink.net._

trait RethinkSpec extends Specification with RQL {
  sequential
  implicit val executor: QueryExecutor
}

trait PlayRethinkSpec extends RethinkSpec with WithData with PlayJsonDef with RQL {
  implicit val executor = new NettyQueryExecutor(RethinkConfig.nettyConfig())
  implicit val bookFormat = play.api.libs.json.Json.format[Book]
}

trait WithData extends RethinkSpec with RQL with BeforeExample {

  lazy val books = r.table("library", "book")
  implicit val bookFormat: JsEncoder[Book] with JsDecoder[Book]

  def book(i: Int) = {
    Book(
      title = "title" + i,
      author = "author" + i,
      price = i.toDouble,
      quantity = i,
      releaseDate = new java.util.Date()
    )
  }

  def before() = {
    val fut = for {
      _ <- r.dbCreate("library").recover{case e => }
      _ <- r.tableCreate("library", "book").recover{case e => }
    } yield true
    fut.await(10)
  }

}
