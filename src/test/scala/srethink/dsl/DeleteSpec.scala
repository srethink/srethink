package srethink.api

class DeleteSpec extends DSLSpec {
  "delete" should {
    "delete data" in {
      persons.insert(man).run must beTrue.await
      persons.get(man.id.get).firstOption[Person] must beSome.await
      persons.delete().run must beTrue.await
      persons.get(man.id.get).firstOption[Person] must beNone.await
    }
  }
}
