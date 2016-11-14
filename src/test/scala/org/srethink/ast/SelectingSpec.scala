package org.srethink.ast

import cats.syntax.either._
import io.circe._
import io.circe.generic.auto._
import java.util.Date

trait SelectingSpec extends AstSpec with SelectingData {
  override def beforeAll(): Unit = {
    super.beforeAll()
    ready(rdb.run(books.insert(book1, book2, book3)))
  }
}



trait SelectingData {

  case class Book(
    id: Long,
    price: Long,
    author: String,
    tags: Seq[String],
    createdAt: Date = new Date
  )

  implicit val encoder: io.circe.Encoder[Date] = new io.circe.Encoder[Date] {
    def apply(d: Date) = {
      val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      Json.fromString(sdf.format(d))
    }
  }

  implicit val decoder: Decoder[Date] = new Decoder[Date] {
    def apply(d: HCursor) = {
      val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      d.as[String].flatMap(str => Either.catchNonFatal(sdf.parse(str)).leftMap(e => DecodingFailure(e.getMessage, d.history)))
    }
  }
  val book1 = Book(1L, 1L, "foo", Seq("foo"))
  val book2 = Book(2L, 2L, "bar", Seq("bar"))
  val book3 = Book(3L, 3L, "baz", Seq("baz"))
}
