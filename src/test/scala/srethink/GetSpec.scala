package srethink

trait GetSpec extends RethinkSpec with WithData {
  "get_all api" should {
    "get all rows of table" in {
      val fut = for{
        ir <- books.insert(Seq(book(1)))
        gr <- books.get(ir.generated_keys(0)).as[Book]
      } yield {
        true
      }
      fut.await
    }
  }
}

class PlayGetSpec extends GetAllSpec with PlayRethinkSpec
