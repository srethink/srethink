package srethink.ast

import scala.collection.immutable.Seq

class InsertSpec extends WithTestTable {
  "insert ast" should {
    "insert object to server" in {
      val data = RObject(Seq("name" -> RStr("foo"), "id" -> RNum(1)))
      expectSuccessAtom(Insert(table, DatumTerm(data)))
    }

    "insert json to server" in {
      val data = RJson("""{"name": "foo", "id": "1"}""")
      expectSuccessAtom(Insert(table, DatumTerm(data)))
    }
  }
}
