package srethink.ast

import AstHelper._

class GetSpec extends WithTestTable {
  "get ast" should {
    "get object by primary key" in new WithTestData {
      expectNotNull(Get(table, numTerm(1)))
      expectNull(Get(table, numTerm(2)))
    }
  }
}
