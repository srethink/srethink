package srethink.api

class GetSpec extends DSLSpec {
  "get" should {
    "insert/get data" in {
      persons.insert(man).run must beTrue.await
      persons.get(man.id.get).first[Person].map(_.id) must be_==(man.id).await
      persons.get(2.0).firstOption[Person] must beNone.await
    }
  }
}
