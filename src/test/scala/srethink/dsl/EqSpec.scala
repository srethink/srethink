package srethink.api

class EqSpec extends DSLSpec {
  "eq function" should {
    "filter fields" in {
      val matchers = for {
        succ <- persons.insert(man).run  if succ
        p <- persons.filter(_.name === man.name).first[Person]
        notExists <- persons.filter(_.name === "balabala").firstOption[Person]
      } yield {
        p.id must be_==(man.id)
        notExists must beNone
      }
      matchers.await
    }
  }
}
