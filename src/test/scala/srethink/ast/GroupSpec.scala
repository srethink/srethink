package srethink.ast

import play.api.rql._
import srethink._

case class GroupResult(group: Int, reduction: Book)
case class Foo(quantity: Int, maxId: String)
class GroupSpec extends RethinkSpec with WithData {
  implicit val grFormat = play.api.libs.json.Json.format[GroupResult]
  implicit val fooFormat = play.api.libs.json.Json.format[Foo]
  val b1 = book(1).copy(id = Some("1"), quantity = 1)
  val b2 = book(2).copy(id = Some("2"), quantity = 2)
  val b3 = book(3).copy(id = Some("3"), quantity = 2)
  "group" should {
    "group and ungroup" in {
      testQuery[Seq[GroupResult]](b1, b2, b3)(books.group("quantity").max("id").ungroup()) {
        case Seq(r1, r2) =>
          val ids = Seq(r1.reduction.id, r2.reduction.id)
          ids must contain(Some("1"))
          ids must contain(Some("3"))
      }
    }
    "group then map" in {
      val rql = books.group("quantity").ungroup().map { gr =>
        r.obj("quantity" -> gr.group, "maxId" -> gr.reduction.max("id").id)
      }
      testQuery[Seq[Foo]](b1, b2, b3)(rql) { foos =>
        foos must contain(Foo(1, "1"))
        foos must contain(Foo(2, "3"))
      }
    }
  }
}
