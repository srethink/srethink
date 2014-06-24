package srethink.ast

import scala.collection.immutable.Seq

class FilterSpec extends WithTestTable {
  "filter ast" should {
    "filter data" in new WithTestData {
      val eq: Int => Func = { id =>
        val argc = DatumTerm(RArray(Seq(RNum(1))))
        val idEq = EQ(GetField(Var(DatumTerm(RNum(1))), DatumTerm(RStr("id"))), DatumTerm(RNum(id)))
        Func(argc, idEq)
      }
      expectNotNull(Filter(table, eq(1)))
    }
  }
}
