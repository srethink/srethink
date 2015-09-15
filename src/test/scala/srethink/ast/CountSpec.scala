package srethink.ast

import srethink._
import play.api.rql._

class CountSpec extends RethinkSpec with WithData {

  val coAuthor1 = CoAuthor(name = "foo", alias = Seq("foo1", "foo2"))
  val coAuthor2 = CoAuthor(name = "bar", alias = Seq("bar1", "bar2"))
  val coAuthors = Seq(coAuthor1, coAuthor2)

  "Count api" should {
    "count docs" in {
      val b1 = book(1)
      val b2 = book(2)
      testQuery[Int](b1, b2)(books.count())(_ == 2)
    }
    "count array field" in {
      val b1 = book(1).copy(id = Some("1"), coAuthors = coAuthors)
      testQuery[Seq[Int]](b1)(books.filter(_.id === "1").map(_.coAuthors.count()))(_ == Seq(2))
    }
  }
}
