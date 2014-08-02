package srethink.api

class SumSpec extends DSLSpec {
  "sum function" should {
    "sum fields with fields" in {
      val matchers = for {
        _ <- persons.insert(man).run
        _ <- persons.insert(boy).run
        sum <- persons.sum("height").first[Int]
      } yield {
        sum must be_==(man.height + boy.height)
      }
      matchers.await
    }
  }
}
