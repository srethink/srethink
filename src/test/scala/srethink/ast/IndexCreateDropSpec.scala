package srethink.ast

import play.api.rql._
import srethink._

class IndexCreateDropSpec extends RethinkSpec with WithData{
  "index create api" should {
    "create index" in {
      val fut = for{
        //drop first
        _ <- books.indexDrop("author").runAs[CreateResult].recover{case e => true}
        cr <- books.indexCreate("author")(_.author).runAs[CreateResult]
        dr <- books.indexDrop("author").runAs[DropResult]
      } yield {
        cr.created must be_==(1)
        dr.dropped must be_==(1)
      }
      fut.await(10)
    }
  }
}
