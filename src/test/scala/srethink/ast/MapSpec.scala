package srethink.ast

import play.api.libs.json._
import play.api.rql._
import srethink._


class MapSpec extends RethinkSpec with WithData {
  "map" should {
    "map result" in {
      val b = book(1)
      val fut = for {
        ir <- books.insert(Seq(b)).runAs[InsertResult]
        qs <- books.getAll(ir.generated_keys.get).map(_.quantity).run
      } yield {
        val expected: JsValue = jsNumber(b.quantity)
        unapplyJsArray(qs) must contain(exactly(expected))
      }
      fut.await
    }
  }
}
