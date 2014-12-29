package srethink.ast

import play.api.rql._
import srethink._

class GetSpec extends RethinkSpec with WithData {
  "get_all api" should {
    "get all rows of table" in {
      val fut = for{
        ir <- books.insert(Seq(book(1))).runAs[InsertResult]
        gr <- books.get(ir.generated_keys.get(0)).runAs[Book]
      } yield {
        true
      }
      fut.await
    }
  }
}
