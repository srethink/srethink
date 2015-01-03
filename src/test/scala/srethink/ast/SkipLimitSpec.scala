package srethink.ast

import srethink._

class SkipLimitSpec extends RethinkSpec with WithData {
  val b1 = book(1).copy(id = Some("1"))
  val b2 = book(2).copy(id = Some("2"))
  "skip limit api" should {
    "skip first doc" in {
      testQuery[Seq[Book]](b1, b2)(books.skip(1))(_.size == 1)
    }
    "limit to first doc" in {
      testQuery[Seq[Book]](b1, b2)(books.limit(1))(_.size == 1)
    }
  }
}
