package srethink

trait IndexCreateDropSpec extends RethinkSpec with WithData{
  "index create api" should {
    "create index" in {
      val fut = for{
        //drop first
        _ <- books.indexDrop("author").recover{case e => true}
        cr <- books.indexCreate("author")(_.author)
        dr <- books.indexDrop("author")
      } yield {
        cr.created must be_==(1)
        dr.dropped must be_==(1)
      }
      fut.await(10)
    }
  }
}

class PlayIndexCreateDropSpec extends IndexCreateDropSpec with PlayRethinkSpec
