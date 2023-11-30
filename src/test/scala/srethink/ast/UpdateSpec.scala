package srethink.ast

import srethink._
import play.api.rql._

class UpdateSpec extends RethinkSpec with WithData {
  test("update docs") {
    val b1 = book(1).copy(id = Some("1"))
    val b2 = book(2).copy(id = Some("2"))
    testQuery[UpdateResult](b1, b2)(books.get("1").update("author" -> "bbb"))(
      _.replaced == 1
    )
    testQuery[Book]()(books.get("1"))(_.author == "bbb")
  }

}
