package srethink.api

class LimitSpec extends DSLSpec {
  "limit dsl" should {
    "limit sequence to specified size" in {
      val count = 10
      val men = (1 to count).map(i => man.copy(id = Some(i)))
      val matchers = for {
        succ <- persons.insert(men: _*).run if succ
        persons <- persons.limit(2).list[Person]
      } yield {
        persons must have size(2)
      }
      matchers.await
    }
  }
}
