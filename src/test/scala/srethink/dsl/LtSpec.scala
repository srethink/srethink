package srethink.api

class LtSpec extends DSLSpec {
  "lt function" should {
    "filter fields" in {
      val matchers = for {
        succ <- persons.insert(man).run  if succ
        p <- persons.filter(_.height < man.height + 1).firstOption[Person]
        notExists <- persons.filter(_.height < man.height - 1).firstOption[Person]
      } yield {
        p must beSome
        notExists must beNone
      }
      matchers.await
    }
  }
}
