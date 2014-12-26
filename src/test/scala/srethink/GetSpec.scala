package srethink

trait GetSpec extends RethinkSpec with WithData {
  "get_all api" should {
    "get all rows of table" in {
      val fut = for{
        ir <- books.insert(Seq(book(1))).runAs[InsertResult]
        gr <- books.get(ir.generated_keys.get(0)).runAs[Book]
      } yield {
        true
      }
      fut.await
    }
  }
}

class PlayGetSpec extends GetAllSpec with PlayRethinkSpec
