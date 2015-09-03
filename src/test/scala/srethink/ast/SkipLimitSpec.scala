package srethink.ast

import srethink._

class SkipLimitSpec extends RethinkSpec with WithData {
  val docs = (1 to 10000).map(i => book(i).copy(id = Some(i.toString)))
  "skip limit api" should {
    "skip first doc" in {
      testQuery[Seq[Book]](docs: _*)(books.skip(1).limit(10000)) { r =>
        println(r.size);
        r.size >= 99
      }
    }
    "limit to first doc" in {
      testQuery[Seq[Book]](docs: _*)(books.limit(1))(_.size == 1)
    }
  }
}
