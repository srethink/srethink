package srethink.dsl

import srethink.api._

class EqSpec extends DSLSpec {
  "eq function" should {
    "filter fields" in {
      val matchers = for {
        succ <- r.table("test").insert(man).run  if succ
        p <- r.table("test").filter(_.name === man.name).first[Person]
        notExists <- r.table("test").filter(_.name === "balabala").first[Person]
      } yield {
        p must beSome
        notExists must beNone
      }
      matchers.await
    }
  }
}
