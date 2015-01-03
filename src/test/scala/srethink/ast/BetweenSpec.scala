package srethink.ast

import srethink._
import play.api.rql._

class BetweenSpec extends RethinkSpec with WithData {
  "Between api" should {
    "query docs between a rang" in {
      val b1 = book(1).copy(id = Some("1"))
      val b2 = book(2).copy(id = Some("2"))
      testQuery[Seq[Book]](b1, b2)(books.between(b1.id.get, b2.id.get)) {
        case Seq(b) => b.id == b1.id
      }
    }
  }
}
