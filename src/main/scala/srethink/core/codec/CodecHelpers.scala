package srethink.core.codec

import srethink.protocol._
import scala.collection.immutable._


object CodecHelper extends Terms with Datums with Queries

trait Terms {
  this: Datums =>
  def term(tpe: Term.TermType.EnumVal, args: Seq[Term], optargs: Seq[Term.AssocPair] = Nil) = {
    Term(
      `type` = Some(tpe),
      `args` = args,
      `optargs` = optargs)
  }

  def insertTerm(table: String, data: Seq[Term], opts: Seq[Term.AssocPair] = Nil) = {
    term(
      tpe = Term.TermType.INSERT,
      args = Seq(
        tableTerm(table),
        makeArrayTerm(data)
      ),
      optargs = opts)
  }

  def datumTerm(datum: Datum) = {
    Term(
      `type` = Some(Term.TermType.DATUM),
      `datum` = Some(datum)
    )
  }

  def tableTerm(name: String) = {
    term(
      tpe = Term.TermType.TABLE,
      args = Seq(datumTerm(strDatum(name)))
    )
  }

  def makeArrayTerm(values: Seq[Term]) = {
    term(
      tpe = Term.TermType.MAKE_ARRAY,
      args = values
    )
  }
}

trait Datums {


  def strDatum(value: String) = {
    Datum(
      `type` = Some(Datum.DatumType.R_STR), `rStr` = Some(value))
  }

  def numDatum(value: Double) = {
    Datum(`type` = Some(Datum.DatumType.R_NUM), rNum = Some(value))
  }

  def boolDatum(value: Boolean) = {
    Datum(`type` = Some(Datum.DatumType.R_BOOL), rBool = Some(value))
  }

  def objDatum(value: Seq[Datum.AssocPair]) = {
    Datum(`type` = Some(Datum.DatumType.R_OBJECT), rObject = value)
  }

}

trait Queries {
  this: Terms =>
  def startQuery(token: Long, query: Term) = {
    Query(
      `token` = Some(token),
      `type` = Some(Query.QueryType.START),
      `query` = Some(query)
    )
  }
}
