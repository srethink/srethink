package srethink.api

class SkipSpec extends DSLSpec {
  "skip dsl" should {
    "skip elements from sequence" in {
      val count = 10
      val men = (1 to count).map(i => man.copy(id = Some(i)))
      val matchers = for {
        succ <- persons.insert(men: _*).run if succ
        ps <- persons.skip(count / 2).list[Person]
      } yield {
        ps must have size(count - count / 2)
      }
      matchers.await
    }
  }
}
