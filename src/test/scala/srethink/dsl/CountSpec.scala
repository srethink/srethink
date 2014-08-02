package srethink.api

class CountSpec extends DSLSpec {
  "count api" should {
    "count sequence" in {
      val count = 100
      val men = (1 to count).map(i => man.copy(id = Some(i)))
      val matchers = for {
        _ <- persons.insert(men: _*).run
        filterdCount <- persons.filter(_.id > count / 2).count.first[Int]
        allCount <- persons.count.first[Int]
      } yield {
        filterdCount must be_==(count / 2)
        allCount must be_==(count)
      }
      matchers.await
    }
  }
}
