package srethink.api

class SubSpec extends DSLSpec {
  "sub function" should {
    "sub fields with fields" in {
      val matchers = for {
        succ <- persons.insert(man).run  if succ
        sub <- persons.map{p => p.height - p.weight}.first[Long]
      } yield {
        sub must be_==(man.height - man.weight.get)
      }
      matchers.await
    }

    "sub fields with number" in {
      val matchers = for {
        succ <- persons.insert(man).run  if succ
        sum <- persons.map{p => p.height - 1000}.first[Long]
      } yield {
        sum must be_==(man.height - 1000)
      }
      matchers.await
    }
  }
}
