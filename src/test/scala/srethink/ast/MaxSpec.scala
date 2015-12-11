package srethink.ast

import play.api.rql._
import srethink._

class MaxSpec extends RethinkSpec with WithData{
  "max api" should {
    "get max by index" in {
      val b1 = book(1).copy(id = Some("1"), quantity = 1)
      val b2 = book(2).copy(id = Some("2"), quantity = 2)
      val b3 = book(3).copy(id = Some("3"), quantity = 3)
      val fut = for{
        //drop first
        _ <- books.indexDrop("quantity").runAs[CreateResult].recover{case e => true}
        cr <- books.indexCreate("quantity")(_.quantity).runAs[CreateResult]
        ir <- {Thread.sleep(3000); books.insert(Seq(b1, b2, b3)).runAs[InsertResult]}
        r <- books.between(0, 100, "index" -> "quantity").maxByIndex("quantity").map(_.quantity).runAs[Seq[Int]]
      } yield {
        cr.created must be_==(1)
        r should have size(1)
      }
      fut.await(10)
    }
  }
}
