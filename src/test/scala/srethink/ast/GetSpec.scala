package srethink.ast

class GetSpec extends WithTestTable {
  "get ast" should {
    "get object by primary key" in new WithTestData {
      expectNotNull(Get(table, DatumTerm(RNum(1))))
      expectNull(Get(table, DatumTerm(RNum(2))))
    }
  }
}
