package srethink.ast

import play.api.rql._
import srethink._
import play.api.libs.json._

case class GroupResult(group: Int, reduction: Book)
case class Foo(quantity: Int, maxId: String)
class GroupSpec extends RethinkSpec with WithData {
  implicit val grFormat: Format[GroupResult] = Json.format[GroupResult]
  implicit val fooFormat: Format[Foo]        = Json.format[Foo]
  val b1 = book(1).copy(id = Some("1"), quantity = 1)
  val b2 = book(2).copy(id = Some("2"), quantity = 2)
  val b3 = book(3).copy(id = Some("3"), quantity = 2)
  test("group and ungroup") {
    testQuery[Seq[GroupResult]](b1, b2, b3)(
      books.group("quantity").max("id").ungroup()
    ) {
      case Seq(r1, r2) =>
        val ids = Seq(r1.reduction.id, r2.reduction.id)
        ids.contains(Some("1")) && ids.contains(Some("3"))
    }
  }
  test("group then map") {
    val rql = books.group("quantity").ungroup().map { gr =>
      r.obj("quantity" -> gr.group, "maxId" -> gr.reduction.max("id").id)
    }
    testQuery[Seq[Foo]](b1, b2, b3)(rql) { foos =>
      foos.contains(Foo(1, "1")) && foos.contains(Foo(2, "3"))
    }
  }
}
