package srethink.dsl

import srethink.api._

class InsertSpec extends DSLSpec {
  "insert" should {
    "insert/get data" in {
      r.table("test").insert(man).run must beTrue.await
    }
  }
}
