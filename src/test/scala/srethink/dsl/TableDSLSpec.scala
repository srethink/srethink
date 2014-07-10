package srethink.dsl

import srethink.api._

class TableDSLSpec extends DSLSpec {
  "table dsl" should {
    "insert/get data" in {
      r.table("test").insert(man).run must beTrue.await
      r.table("test").get(1).first[Person].map(_.map(_.id)) must beSome(man.id).await
      r.table("test").get(2).first[Person] must beNone.await
    }
  }
}
