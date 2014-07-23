package srethink.dsl

import srethink.api._

class CountSpec extends DSLSpec {
  "count api" should {
    "count sequence" in {
      val count = 100
      val men = (1 to count).map(i => man.copy(id = Some(i)))
      val matchers = for {
        _ <- r.table("test").insert(men: _*).run
        filterdCount <- r.table("test").filter(_.id > count / 2).count.first[Int]
        allCount <- r.table("test").count.first[Int]
      } yield {
        filterdCount must beSome(count / 2)
        allCount must beSome(count)
      }
      matchers.await
    }
  }
}
