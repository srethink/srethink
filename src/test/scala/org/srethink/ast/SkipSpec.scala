package org.srethink.ast

import io.circe.generic.auto._

class SkipSpec extends SelectingSpec {
  "Skip" should "skip n items" in {
    rdb.run[Book](books.skip(1).limit(3)).map(_ should have size(2))
  }
}
