package srethink.ast

import srethink._
import play.api.rql._

class ReplaceSpec extends RethinkSpec with WithData {
  "relace api" should {
    "replace docs" in {

      val b1 = book(1).copy(id = Some("1"))
      val b2 = book(2).copy(id = Some("2"))
      println(books.get("1").replace(b1.copy(title = "replaced")).term)
      testQuery[UpdateResult](b1, b2)(books.get("1").replace(b1.copy(title = "replaced")))(_.replaced == 1)
      testQuery[Book]()(books.get("1"))(_.title == "replaced")
    }
  }
}
