package srethink.ast

import srethink._
import play.api.rql._
import scala.concurrent.ExecutionContext.Implicits.global

class GetAllSpec extends RethinkSpec with WithData {
  test("get all rows of table") {
    for {
      ir <- books.insert(Seq(book(1))).runAs[InsertResult]
      gr <- books.getAll(ir.generated_keys.get).runAs[Seq[Book]]
    } yield {
      assertEquals(gr.size, 1)
    }
  }
}
