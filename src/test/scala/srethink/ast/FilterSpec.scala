package srethink.ast

import srethink._

class FilterSpec extends RethinkSpec with WithData {
  val b1 = book(1).copy(id = Some("1"))
  val b2 = book(2).copy(id = Some("2"))
  val b3 = book(3).copy(id = Some("3"))
  "filter api" should {
    "filter doc with id >= 2" in {
      testQuery[Seq[Book]](b1, b2, b3)(books.filter(_.seq >= 2))(_.size == 2)
    }
    "filter doc with id <= 2" in {
      testQuery[Seq[Book]](b1, b2, b3)(books.filter(_.seq <= 2))(_.size == 2)
    }

    "filter doc with id > 2" in {
      testQuery[Seq[Book]](b1, b2, b3)(books.filter(_.seq > 2))(_.size == 1)
    }

    "filter doc with id < 2" in {
      testQuery[Seq[Book]](b1, b2, b3)(books.filter(_.seq < 2))(_.size == 1)
    }
    "filter doc with id in 2 or 3" in {
      testQuery[Seq[Book]](b1, b2, b3)(books.filter(b => b.seq === 2 || b.seq === 1))(_.size == 2)
    }
  }
}
