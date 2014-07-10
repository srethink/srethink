package srethink.dsl

import srethink.api._

class SequenceDSLSpec extends DSLSpec {
  "sequence dsl" should {
    "filter data" in {
      val matchers = for {
        succ <- r.table("test").insert(man).run  if succ
        person <- r.table("test").filter(_.name === "man").first[Person]
        notExists <- r.table("test").filter(_.name === "woman").first[Person]
      } yield {
        person.map(_.id) must beSome(man.id)
        notExists must beNone
      }
      matchers.await
    }

    "map data" in {
      val matchers = for {
        succ <- r.table("test").insert(man).run  if succ
        h <- r.table("test").map(_.height - 100).first[Long]
      } yield {
        h must beSome(man.height - 100)
      }
      matchers.await
    }
  }
}
