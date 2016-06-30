package org.srethink.ast

class CountSpec extends SelectingSpec {
  "Count" should "count books" in {
    rdb.run[Int](books.count()).map(_ shouldEqual(3))
  }
}
