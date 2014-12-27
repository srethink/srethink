package srethink.ast

import play.api.rql._
import srethink._

class InsertSpec extends RethinkSpec with WithData {
  "insert api" should {
    "insert scala values" in {
      val items = (1 to 1).map(book)
      books.insert(items).runAs[InsertResult].map(_.inserted) must be_==(items.size).await
    }

    "insert JsValues value" in {
      val items = Seq(jsBook(1), jsBook(2))
      val ir = books.insertJS(items).runAs[InsertResult]
      ir.map(_.inserted) must be_==(2).await
    }
  }
}
