package srethink.ast

import srethink._

class SkipLimitSpec extends RethinkSpec with WithData {
  val b1 = book(1).copy(id = Some("1"))
  val b2 = book(2).copy(id = Some("2"))
  test("skip first doc")  {
    testQuery[Seq[Book]](b1, b2)(books.skip(1))(_.size == 1)
  }
  test("limit to first doc") {
    testQuery[Seq[Book]](b1, b2)(books.limit(1))(_.size == 1)
  }

}
