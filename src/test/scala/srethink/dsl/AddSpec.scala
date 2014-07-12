package srethink.dsl

import srethink.api._

class AddSpec extends DSLSpec {
  "add function" should {
    "add fields with fields" in {
      val matchers = for {
        succ <- r.table("test").insert(man).run  if succ
        sum <- r.table("test").map{p => p.height + p.weight}.first[Long]
      } yield {
        sum must beSome(man.height + man.weight.get)
      }
      matchers.await
    }

    "add fields with number" in {
      val matchers = for {
        succ <- r.table("test").insert(man).run  if succ
        sum <- r.table("test").map{p => p.height + 1000}.first[Long]
      } yield {
        sum must beSome(man.height + 1000)
      }
      matchers.await
    }
  }
}
