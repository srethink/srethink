package srethink.ast

import java.text.SimpleDateFormat
import java.util.Date
import srethink.protocol._
import srethink.protocol.Datum.DatumType
import srethink.protocol.Term.TermType
import scala.collection.{Seq => _}
import scala.language.existentials
import AstHelper._

trait RDatum extends Any{
  def toDatum: Datum
}


class RBool(val value: Boolean) extends AnyVal with RDatum {
  def toDatum = Datum(
    `type` = Some(DatumType.R_BOOL),
    rBool = Some(value)
  )
}
class RNum(val value: Double) extends AnyVal with RDatum {
  def toDatum = Datum(
    `type` = Some(DatumType.R_NUM),
    rNum = Some(value)
  )
}

class RStr(val value: String) extends AnyVal with RDatum {
  def toDatum = Datum(
    `type` = Some(DatumType.R_STR),
    rStr = Some(value)
  )
}

class RArray(val value: Seq[RDatum]) extends AnyVal with RDatum {
  def toDatum = Datum(
    `type` = Some(DatumType.R_ARRAY),
    rArray = value.map(_.toDatum))
}

class RObject(val value: Seq[(String, RDatum)]) extends AnyVal with RDatum {

  def toDatum = {
    val pairs = value.map {
      case (name, data) => Datum.AssocPair(Some(name), Some(data.toDatum))
    }
    Datum(
      `type` = Some(DatumType.R_OBJECT),
      rObject = pairs
    )
  }
}

class RJson(val value: String) extends AnyVal with RDatum {
  def toDatum = Datum(
    `type` = Some(DatumType.R_JSON),
    rStr = Some(value)
  )
}

trait RTerm extends Any {
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

class DatumTerm[T <: RDatum](val rDatum: T) extends AnyVal with RTerm {
  def toTerm = Term(Some(TermType.DATUM),  Some(rDatum.toDatum))
}


case class RTable(
  name: String,
  rdb: Option[RDb] = None,
  opts: RTermOpts = RTerm.REmptyOpts) extends RTerm {
  def toTerm = Term(
    `type` = Some(TermType.TABLE),
    `args` = Seq(strTerm(name).toTerm),
    optargs = opts.toOptArgs)
}

class RDb(val name: String) extends AnyVal with RTerm {
  def toTerm = Term(
    `type` = Some(TermType.DB),
    `args` = Seq(strTerm(name).toTerm)
  )
}

class RMakeArray(val terms: Seq[RTerm]) extends AnyVal with RTerm {
  def toTerm = Term(
    `type` = Some(TermType.MAKE_ARRAY),
    args = terms.map(_.toTerm)
  )
}

class ISO8601(val time: Date) extends AnyVal with RTerm {
  def toTerm = {
    val str = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(time)
    Term(
      `type` = Some(TermType.ISO8601),
      args = Seq(strTerm(str).toTerm))
  }
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

class Var(val id: Int) extends AnyVal with RTerm {
  def toTerm = Term(
    `type` = Some(TermType.VAR),
    args = Seq(numTerm(id).toTerm)
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

case class LT(left: RTerm, right: RTerm) extends RPredicate {
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
