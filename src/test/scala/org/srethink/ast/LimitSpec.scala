package org.srethink.ast

import io.circe.generic.auto._

class LimitSpec extends SelectingSpec {
  "Take" should "take n items" in {
    rdb.run[Book](books.limit(1)).map(_ should have size(1))
  }
}
