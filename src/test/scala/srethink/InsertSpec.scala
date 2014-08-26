package srethink

trait InsertSpec extends RethinkSpec with WithData {
  "insert api" should {
    "insert data" in {
      val items = (1 to 1).map(book)
      books.insert(items).map(_.inserted) must be_==(items.size).await
    }
  }
}

class PlayInsertSpec extends InsertSpec with PlayRethinkSpec
