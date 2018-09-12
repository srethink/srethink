package org.srethink.ast

import cats.syntax.either._
import io.circe.generic.auto._

class UpdateSpec extends SelectingSpec {
  case class UpdateBook(coauthor: Seq[String])
  "Update" should "update single item" in {
    val action = books.get(1).update(UpdateBook(Seq("foo", "bar")))
    rdb.run(action).collect {
      case Right(json) =>
        json.hcursor.downField("replaced").as[Int] shouldEqual(Either.right(1))
        succeed
    }
  }

  it should "update multi items" in {
    val action = books.update(UpdateBook(Seq("bar", "baz")))
    rdb.run(action).map {
      case Right(j) =>
        j.hcursor.downField("replaced").as[Int] shouldEqual Either.right(3)
      case e =>
        fail(e.toString)
    }
  }
}
