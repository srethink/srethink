package srethink.dsl

import srethink.api._

class NotSpec extends DSLSpec {
  "not dsl" should {
    "filter elements that are not studends" in {
      val matchers = for {
        succ <- r.table("test").insert(man).run  if succ
        p <- r.table("test").filter(!_.isStudent).first[Person]
        notExists <- r.table("test").filter(_.isStudent).first[Person]
      } yield {
        p must beSome
        notExists must beNone
      }
      matchers.await
    }
  }
}
