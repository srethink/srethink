package srethink.dsl

import srethink.api._

class SubSpec extends DSLSpec {
  "sub function" should {
    "sub fields with fields" in {
      val matchers = for {
        succ <- r.table("test").insert(man).run  if succ
        sub <- r.table("test").map{p => p.height - p.weight}.first[Long]
      } yield {
        sub must be_==(man.height - man.weight.get)
      }
      matchers.await
    }

    "sub fields with number" in {
      val matchers = for {
        succ <- r.table("test").insert(man).run  if succ
        sum <- r.table("test").map{p => p.height - 1000}.first[Long]
      } yield {
        sum must be_==(man.height - 1000)
      }
      matchers.await
    }
  }
}
