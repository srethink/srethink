package srethink.dsl

import srethink.api._

class SumFuncSpec extends DSLSpec {
  "sum function" should {
    "sum fields with fields" in {
      val matchers = for {
        _ <- r.table("test").insert(man).run
        _ <- r.table("test").insert(boy).run
        sum <- r.table("test").sum("height").first[Int]
      } yield {
        sum must beSome(man.height + boy.height)
      }
      matchers.await
    }
  }
}
