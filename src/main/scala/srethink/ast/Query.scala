package srethink.ast

case class RDynamic(term: RTerm) extends Dynamic with RTerm {

  def selectDynamic(name: String) = {
    RDynamic(GetField(term, DatumTerm(RStr(name))))
  }

  def toTerm = term.toTerm
}

class RTermOps(val term: RTerm) extends AnyVal {
  def === (that: RTerm) = EQ(term, that)
  def > (that: RTerm) = GT(term, that)
  def < (that: RTerm) = LT(term, that)
}

trait QueryImplicits {
  implicit def term2RTermOps(term: RTerm) = new RTermOps(term)
}
