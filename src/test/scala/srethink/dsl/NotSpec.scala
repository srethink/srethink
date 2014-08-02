package srethink.api

class NotSpec extends DSLSpec {
  "not dsl" should {
    "filter elements that are not studends" in {
      val matchers = for {
        succ <- persons.insert(man).run  if succ
        p <- persons.filter(!_.isStudent).firstOption[Person]
        notExists <- persons.filter(_.isStudent).firstOption[Person]
      } yield {
        p must beSome
        notExists must beNone
      }
      matchers.await
    }
  }
}
