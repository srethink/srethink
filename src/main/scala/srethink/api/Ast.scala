package srethink.api

import srethink.protocol._
import scala.collection.immutable.Seq
import scala.collection.{Seq => _}

trait RDatum {
  def toDatum: Datum
}

case class RBool(value: Boolean) extends RDatum {
  def toDatum = Datum(
    `type` = Some(Datum.DatumType.R_BOOL),
    rBool = Some(value)
  )
}
case class RNum(value: Double) extends RDatum {
  def toDatum = Datum(
    `type` = Some(Datum.DatumType.R_NUM),
    rNum = Some(value)
  )
}

case class RStr(value: String) extends RDatum {
  def toDatum = Datum(
    `type` = Some(Datum.DatumType.R_STR),
    rStr = Some(value)
  )
}

case class RArray(value: Seq[RDatum]) extends RDatum {
  def toDatum = Datum(
    `type` = Some(Datum.DatumType.R_ARRAY),
    rArray = value.map(_.toDatum))
}

case class RObject(value: Seq[(String, RDatum)]) extends RDatum {
  val pairs = value.map {
    case (name, data) => Datum.AssocPair(Some(name), Some(data.toDatum))
  }
  def toDatum = Datum(
    `type` = Some(Datum.DatumType.R_OBJECT),
    rObject = pairs
  )
}

trait RTerm {
  def toTerm: Term
}

case class DatumTerm(rDatum: RDatum) extends RTerm {
  def toTerm = Term(Some(Term.TermType.DATUM),  Some(rDatum.toDatum))
}


case class RTable(name: DatumTerm) extends RTerm {
  def toTerm = Term(
    `type` = Some(Term.TermType.TABLE),
    `args` = Seq(name.toTerm)
  )
}

case class RDb(name: DatumTerm) extends RTerm {
  def toTerm = Term(
    `type` = Some(Term.TermType.DB),
    `args` = Seq(name.toTerm)
  )
}



case class MakeArray(terms: Seq[RTerm]) extends RTerm {
  def toTerm = Term(
    `type` = Some(Term.TermType.MAKE_ARRAY),
    args = terms.map(_.toTerm)
  )
}

case class Get(table: RTable, primaryKey: DatumTerm) {
  def toTerm = Term(
    `type` = Some(Term.TermType.GET),
    args = Seq(table.toTerm, primaryKey.toTerm)
  )
}
