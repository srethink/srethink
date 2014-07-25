package srethink.dsl

import srethink.api._

class DuringSpec extends DSLSpec {
  "during api" should {
    "filter data with time range" in {
      val matchers = for {
        succ <- r.table("test").insert(man, women, boy).run  if succ
        b <- r.table("test").filter(_.birth.during(yearsAgo(11), yearsAgo(9))).first[Person]
        m <- r.table("test").filter(_.birth.during(yearsAgo(31), yearsAgo(29))).first[Person]
      } yield {
        b.id must be_==(boy.id)
        m.id must be_==(man.id)
      }
      matchers.await
    }
  }
}
