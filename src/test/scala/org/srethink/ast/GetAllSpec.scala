package org.srethink.ast

import io.circe.generic.auto._

class GetAllSpec extends SelectingSpec {
  "GetAll" should "get all elements" in {
    rdb.run[Book](books.getAll(Seq(1L, 2L, 3L))).map(_ should have size(3))
  }
}
