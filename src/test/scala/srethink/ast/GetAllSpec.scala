package srethink.ast

import srethink._
import play.api.rql._

class GetAllSpec extends RethinkSpec with WithData {
  "get_all api" should {
    "get all rows of table" in {
      val fut = for {
        ir <- books.insert(Seq(book(1), book(2))).runAs[InsertResult]
        gr <- books.getAll(ir.generated_keys.get).limit(2).runAs[Seq[Book]]
      } yield {
        gr must have size(2)
      }
      fut.await
    }
  }
}
