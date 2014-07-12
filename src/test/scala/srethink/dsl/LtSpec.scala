package srethink.dsl

import srethink.api._

class LtSpec extends DSLSpec {
  "lt function" should {
    "filter fields" in {
      val matchers = for {
        succ <- r.table("test").insert(man).run  if succ
        p <- r.table("test").filter(_.height < man.height + 1).first[Person]
        notExists <- r.table("test").filter(_.height < man.height - 1).first[Person]
      } yield {
        p must beSome
        notExists must beNone
      }
      matchers.await
    }
  }
}
