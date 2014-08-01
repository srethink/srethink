package srethink.api

class DuringSpec extends DSLSpec {
  "during api" should {
    "filter data with time range" in {
      val matchers = for {
        succ <- persons.insert(man, women, boy).run  if succ
        b <- persons.filter(_.birth.during(yearsAgo(11), yearsAgo(9))).first[Person]
        m <- persons.filter(_.birth.during(yearsAgo(31), yearsAgo(29))).first[Person]
      } yield {
        b.id must be_==(boy.id)
        m.id must be_==(man.id)
      }
      matchers.await
    }
  }
}
