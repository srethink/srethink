package srethink.dsl

import srethink.api._

class LimitSpec extends DSLSpec {
  "limit dsl" should {
    "limit sequence to specified size" in {
      val count = 10
      val men = (1 to count).map(i => man.copy(id = Some(i)))
      val matchers = for {
        succ <- r.table("test").insert(men: _*).run if succ
        persons <- r.table("test").limit(2).list[Person]
      } yield {
        persons must have size(2)
      }
      matchers.await
    }
  }
}
