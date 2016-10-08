package org.srethink.ast


class MapSpec extends SelectingSpec {
  "Map" should "get ids" in {
    rdb.run[Long](books.getAll(Seq(1L, 2L, 3L)).map(_.id)).map(_ should have size(3))
  }
}
