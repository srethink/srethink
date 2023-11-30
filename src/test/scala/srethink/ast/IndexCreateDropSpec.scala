package srethink.ast

import play.api.rql._
import srethink._
import scala.concurrent.ExecutionContext.Implicits.global

class IndexCreateDropSpec extends RethinkSpec with WithData {
  test("create index") {
    for {
      // drop first
      _ <- books.indexDrop("author").runAs[CreateResult].recover {
        case e => true
      }
      cr <- books.indexCreate("author")(_.author).runAs[CreateResult]
      dr <- books.indexDrop("author").runAs[DropResult]
    } yield {
      assertEquals(cr.created, 1)
      assertEquals(dr.dropped, 1)
    }
  }
}
