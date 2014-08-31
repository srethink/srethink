package srethink

trait InsertSpec extends RethinkSpec with WithData {
  "insert api" should {
    "insert scala values" in {
      val items = (1 to 1).map(book)
      books.insert(items).map(_.inserted) must be_==(items.size).await
    }

    "insert JsValues value" in {
      val items = jsArray(Seq(jsBook(1), jsBook(2)))
      val ir = books.insert(items)
      ir.map(_.inserted) must be_==(2).await
    }
  }
}

class PlayInsertSpec extends InsertSpec with PlayRethinkSpec
