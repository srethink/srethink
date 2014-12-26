package srethink

trait GetAllSpec extends RethinkSpec with WithData {
  "get_all api" should {
    "get all rows of table" in {
      val fut = for {
        ir <- books.insert(Seq(book(1))).runAs[InsertResult]
        gr <- books.getAll(ir.generated_keys.get).runAs[Seq[Book]]
      } yield {
        gr must have size(1)
      }
      fut.await
    }
  }
}

class PlayGetAllSpec extends GetAllSpec with PlayRethinkSpec
