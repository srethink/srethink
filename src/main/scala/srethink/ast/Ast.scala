package srethink.ast

import srethink.protocol._
import srethink.protocol.Datum.DatumType
import srethink.protocol.Term.TermType
import scala.collection.immutable.Seq
import scala.collection.{Seq => _}
import scala.language.existentials

trait RDatum {
  def toDatum: Datum
}


case class RBool(value: Boolean) extends RDatum {
  def toDatum = Datum(
    `type` = Some(DatumType.R_BOOL),
    rBool = Some(value)
  )
}
case class RNum(value: Double) extends RDatum {
  def toDatum = Datum(
    `type` = Some(DatumType.R_NUM),
    rNum = Some(value)
  )
}

case class RStr(value: String) extends RDatum {
  def toDatum = Datum(
    `type` = Some(DatumType.R_STR),
    rStr = Some(value)
  )
}

case class RArray(value: Seq[RDatum]) extends RDatum {
  def toDatum = Datum(
    `type` = Some(DatumType.R_ARRAY),
    rArray = value.map(_.toDatum))
}

case class RObject(value: Seq[(String, RDatum)]) extends RDatum {
  val pairs = value.map {
    case (name, data) => Datum.AssocPair(Some(name), Some(data.toDatum))
  }
  def toDatum = Datum(
    `type` = Some(DatumType.R_OBJECT),
    rObject = pairs
  )
}

case class RJson(value: String) extends RDatum {
  def toDatum = Datum(
    `type` = Some(DatumType.R_JSON),
    rStr = Some(value)
  )
}

trait RTerm {
  def toTerm: Term
}

trait RPredicate extends RTerm

object RTerm {
  val REmptyOpts = new RTermOpts(Nil)
}

class RTermOpts(val opts: Seq[(String, RTerm)]) extends AnyVal {
  def toOptArgs = opts.map {
    case (name, value) => Term.AssocPair(Some(name), Some(value.toTerm))
  }
}

object RTermOpts {
  def apply(opts: (String, RTerm)*) = new RTermOpts(opts.to[Seq])
}


case class DatumTerm[T <: RDatum](rDatum: T) extends RTerm {
  def toTerm = Term(Some(TermType.DATUM),  Some(rDatum.toDatum))
}


case class RTable(
  name: DatumTerm[RStr],
  rdb: Option[RDb] = None,
  opts: RTermOpts = RTerm.REmptyOpts) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.TABLE),
    `args` = Seq(name.toTerm),
    optargs = opts.toOptArgs)
}

case class RDb(name: DatumTerm[RStr]) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.DB),
    `args` = Seq(name.toTerm)
  )
}

case class RMakeArray(terms: Seq[RTerm]) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.MAKE_ARRAY),
    args = terms.map(_.toTerm)
  )
}

case class ISO8601(time: DatumTerm[RStr]) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.ISO8601),
    args = Seq(time.toTerm)
  )
}

case class Get(table: RTable, primaryKey: DatumTerm[_ <: RDatum]) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.GET),
    args = Seq(table.toTerm, primaryKey.toTerm)
  )
}

case class DBCreate(db: DatumTerm[RStr]) extends RTerm {
  def toTerm = Term(`type` = Some(TermType.DB_CREATE), args = Seq(db.toTerm))
}

case class DBDrop(db: DatumTerm[RStr]) extends RTerm {
  def toTerm = Term(`type` = Some(TermType.DB_DROP), args = Seq(db.toTerm))
}

case class TableCreate(table: DatumTerm[RStr], opts: RTermOpts = RTerm.REmptyOpts) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.TABLE_CREATE),
    args = Seq(table.toTerm)
  )
}

case class TableDrop(table: DatumTerm[RStr], db: Option[RDb] = None) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.TABLE_DROP),
    args = Seq(table.toTerm)
  )
}

case class Insert[T <: RDatum]( table: RTable, data: DatumTerm[T], db: Option[RDb] = None) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.INSERT),
    `args` = Seq(table.toTerm, data.toTerm)
  )
}

case class Var(name: DatumTerm[RNum]) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.VAR),
    args = Seq(name.toTerm)
  )
}

case class Func(argc: DatumTerm[RArray], body: RTerm) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.FUNC),
    args = Seq(argc.toTerm, body.toTerm)
  )
}

case class EQ(items: RTerm*) extends RPredicate {
  def toTerm = Term(
    `type` = Some(TermType.EQ),
    args = items.map(_.toTerm).to[Seq]
  )
}

case class GT(left: RTerm, right: RTerm) extends RPredicate {
  def toTerm = Term(
    `type` = Some(TermType.GT),
    args = Seq(left.toTerm, right.toTerm)
  )
}

case class LT(left: RTerm, right: RTerm) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.LT),
    args = Seq(left.toTerm, right.toTerm)
  )
}

case class Filter(sequence: RTerm, func: RTerm) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.FILTER),
    args = Seq(sequence.toTerm, func.toTerm)
  )
}

case class Map(sequence: RTerm, func: RTerm) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.MAP),
    args = Seq(sequence.toTerm, func.toTerm)
  )
}

case class GetField(obj: RTerm, name: DatumTerm[RStr]) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.GET_FIELD),
    `args` = Seq(obj.toTerm, name.toTerm)
  )
}
