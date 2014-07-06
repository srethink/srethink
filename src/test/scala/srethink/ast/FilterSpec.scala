package srethink.ast

import AstHelper._

class FilterSpec extends WithTestTable {
  "filter ast" should {
    "filter data" in new WithTestData {
      val eq: Int => Func = { id =>
        val argc = new DatumTerm(new RArray(Seq(new RNum(1))))
        val idEq = EQ(GetField(new Var(1), strTerm("id")), numTerm(id))
        Func(argc, idEq)
      }
      expectNotNull(Filter(table, eq(1)))
    }
  }
}
