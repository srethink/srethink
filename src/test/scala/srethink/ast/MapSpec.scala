package srethink.ast

import play.api.libs.json._
import play.api.rql._
import srethink._
import scala.concurrent.ExecutionContext.Implicits.global

class MapSpec extends RethinkSpec with WithData {

  test("map result")  {
    val b = book(1)
    for {
      ir <- books.insert(Seq(b)).runAs[InsertResult]
      qs <- books.getAll(ir.generated_keys.get).map(_.quantity).run
    } yield {
      val expected: JsValue = jsNumber(b.quantity)
      assert(unapplyJsArray(qs).contains(expected))
    }
  }
}
