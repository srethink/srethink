package org.srethink.ast

import io.circe.generic.auto._
import org.srethink._

class FilterSpec extends SelectingSpec {
  "Filter" should "filter by id" in {
    rdb.run[Book](books.filter(_.id > 1L).filter(_.id > 2L)).map(_ should have size(1))
  }
}
