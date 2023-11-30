package srethink.ast

import play.api.rql._
import srethink._
import scala.concurrent.ExecutionContext.Implicits.global

class DeleteSpec extends RethinkSpec with WithData {
  test("delete all elements of table") {
    val items = (1 to 1).map(book)
    for {
      ir <- books.insert(items).runAs[InsertResult]
      dr <- books.delete().runAs[DeleteResult]
    } yield {
      assertEquals(ir.inserted, items.size)
      assertEquals(dr.deleted, items.size)
    }
  }

}
