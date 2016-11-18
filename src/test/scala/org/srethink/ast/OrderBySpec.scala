package org.srethink.ast

import io.circe.generic.auto._
import org.srethink._

class OrderBySpec extends SelectingSpec {
  "Filter" should "filter by id" in {
    rdb.run[Book](books.orderBy(r.asc("id"))).map { bs =>
      bs.map(_.id) shouldEqual(List(1, 2, 3))
    }
    rdb.run[Book](books.orderBy(r.desc("id"))).map { bs =>
      bs.map(_.id) shouldEqual(List(3, 2, 1))
    }
  }
}
