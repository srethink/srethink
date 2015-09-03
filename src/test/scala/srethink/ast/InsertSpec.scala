package srethink.ast

import play.api.rql._
import srethink._
import play.api.libs.json._

class InsertSpec extends RethinkSpec with WithData {
  "insert api" should {
    "insert scala values" in {
      val items = (1 to 1).map(book)
      books.insert(items).runAs[InsertResult].map(_.inserted) must be_==(items.size).await
    }

    "insert JsValues value" in {
      val items = Seq(Json.parse(json))

      val ir = books.insert(items).runAs[InsertResult]
      ir.map(_.inserted) must be_==(2).await
    }

    def json = """
{"id":13349038,"formId":"7cf56f95-d983-4247-9479-5aac07388c7d","answers":[2, [{"fieldValues":[2, [29,10]]}]],"isNotified":true}
"""
  }
}
