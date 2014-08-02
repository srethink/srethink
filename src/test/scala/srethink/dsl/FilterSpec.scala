package srethink.api

class FilterSpec extends DSLSpec {
  "filter" should {
    "filter all type of data" in {
      val matchers = for {
        succ <- persons.insert(man).run  if succ
        longF <- persons.filter(_.id === man.id.get).firstOption[Person]
        strF <- persons.filter(_.name === man.name).firstOption[Person]
        intF <- persons.filter(_.height === man.height).firstOption[Person]
        doubleF <- persons.filter(_.salary === man.salary).firstOption[Person]
        floatF <- persons.filter(_.workHours === man.workHours).firstOption[Person]
        boolF <- persons.filter(_.isStudent === man.isStudent).firstOption[Person]
      } yield {
        longF must beSome
        strF must beSome
        intF must beSome
        doubleF must beSome
        floatF must beSome
        boolF must beSome
      }
      matchers.await
    }
  }

}
