package srethink.api

import srethink.ast._

trait QueryDSL {
  implicit class RDynamic(val term: RTerm) extends Dynamic {
    def selectDynamic(name: String) = {
      RDynamic(GetField(term, DatumTerm(RStr(name))))
    }
  }

  implicit class RTermOps(val term: RTerm) {
    def === (that: RTerm) = EQ(term, that)
    def > (that: RTerm) = GT(term, that)
    def < (that: RTerm) = LT(term, that)
  }
}
