package srethink.ast

import srethink._

class InsertSpec extends WithTestTable {
  "insert ast" should {
    "insert object to server" in {
      val data = new RObject(Seq("name" -> new RStr("foo"), "id" -> new RNum(1)))
      expectSuccessAtom(Insert(table, new DatumTerm(data)))
    }

    "insert json to server" in {
      val data = new RJson("""{"name": "foo", "id": "1"}""")
      expectSuccessAtom(Insert(table, new DatumTerm(data)))
    }
  }
}
