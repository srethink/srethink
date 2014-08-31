package srethink

trait DeleteSpec extends RethinkSpec with WithData {
  "delete api" should {
    "delete all elements of table" in {
      val items = (1 to 1).map(book)
      val ir = books.insert(items)
      ir.map(_.inserted) must be_==(items.size).await
      val dr = books.delete()
      dr.map(_.deleted)  must be_==(items.size).await
    }
  }
}

class PlayDeleteSpec extends DeleteSpec with PlayRethinkSpec
