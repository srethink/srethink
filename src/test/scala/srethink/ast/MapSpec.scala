package srethink.ast

import srethink._

trait MapSpec extends RethinkSpec with WithData {
  "map" should {
    "map result" in {
      val b = book(1)
      val fut = for {
        ir <- books.insert(Seq(b)).runAs[InsertResult]
        qs <- books.getAll(ir.generated_keys.get).map(_.quantity).run
      } yield {
        val expected: JsValue = jsNumber(b.quantity)
        exactJsArray(qs) must contain(exactly(expected))
      }
      fut.await
    }
  }
}

class PlayMapSpec extends MapSpec with PlayRethinkSpec
