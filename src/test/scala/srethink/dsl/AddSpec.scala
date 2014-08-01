package srethink.api

class AddSpec extends DSLSpec {
  "add function" should {
    "add fields with fields" in {
      val matchers = for {
        succ <- persons.insert(man).run  if succ
        sum <- persons.map{p => p.height + p.weight}.first[Int]
      } yield {
        sum must be_==(man.height + man.weight.get)
      }
      matchers.await
    }

    "add fields with number" in {
      val matchers = for {
        succ <- persons.insert(man).run  if succ
        sum <- persons.map{p => p.height + 1000}.first[Int]
      } yield {
        sum must be_==(man.height + 1000)
      }
      matchers.await
    }
  }
}
