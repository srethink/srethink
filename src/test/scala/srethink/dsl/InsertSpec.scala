package srethink.api

class InsertSpec extends DSLSpec {
  "insert" should {
    "insert/get data" in {
      persons.insert(man).run must beTrue.await
    }
  }
}
