package srethink.ast

import srethink._
import play.api.rql._

class OrderBySpec extends RethinkSpec with WithData {
  val b1 = book(1).copy(id = Some("1"))
  val b2 = book(2).copy(id = Some("2"))
  val b3 = book(3).copy(id = Some("3"))
  "order by api" should {
    "order the elements" in {
      testQuery[Seq[Book]](b1, b2, b3)(books.orderBy(r.desc("id")).limit(1))(_.head.id == Some("3"))
    }

  }
}
