package srethink.dsl

import srethink.api._

class EqSpec extends DSLSpec {
  "eq function" should {
    "filter fields" in {
      val matchers = for {
        succ <- r.table("test").insert(man).run  if succ
        p <- r.table("test").filter(_.name === man.name).first[Person]
        notExists <- r.table("test").filter(_.name === "balabala").firstOption[Person]
      } yield {
        p.id must be_==(man.id)
        notExists must beNone
      }
      matchers.await
    }
  }
}
