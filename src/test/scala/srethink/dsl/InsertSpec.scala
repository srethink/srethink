package srethink.api

class InsertSpec extends DSLSpec {
  "insert" should {
    "insert/get data" in {
      persons.insert(man).run must beTrue.await
    }

    "insert without primary key" in {
      val man1 = man.copy(id = None)
      persons.insert(man).run must beTrue.await
    }
  }
}
