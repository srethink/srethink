package srethink.ast

import play.api.rql._
import srethink._
import scala.concurrent.ExecutionContext.Implicits.global

class GetSpec extends RethinkSpec with WithData {
  for{
    ir <- books.insert(Seq(book(1))).runAs[InsertResult]
    gr <- books.get(ir.generated_keys.get(0)).runAs[Book]
  } yield true
}
