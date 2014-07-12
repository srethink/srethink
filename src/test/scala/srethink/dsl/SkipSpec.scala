package srethink.dsl

import srethink.api._

class SkipSpec extends DSLSpec {
  "skip dsl" should {
    "skip elements from sequence" in {
      val count = 10
      val men = (1 to count).map(i => man.copy(id = Some(i)))
      val matchers = for {
        succ <- r.table("test").insert(men: _*).run if succ
        persons <- r.table("test").skip(count / 2).list[Person]
      } yield {
        persons must have size(count - count / 2)
      }
      matchers.await
    }
  }
}
