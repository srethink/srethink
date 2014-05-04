package srethink.core

import srethink.protocol._
import scala.collection.immutable._

trait DatumDecoder[T] {
  def decode(data: Datum): Option[T]
}

trait DatumEncoder[T] {
  def encode(t: T): Datum
}

trait QueryEncoder[T] {
  def encode(token: Long, api: T): Query
}

trait BaseicDatumDecoders {
  implicit object IntDecoder extends DatumDecoder[Int] {
    def decode(datum: Datum) = {
      for{
        t <- datum.`type` if t == Datum.DatumType.R_NUM
        v <- datum.rNum
      } yield v.toInt
    }
  }

  implicit object StringDecoder extends DatumDecoder[String] {
    def decode(datum: Datum) = {
      for{
        t <- datum.`type` if t == Datum.DatumType.R_STR
        v <- datum.rStr
      } yield v
    }
  }
}

object CodecHelper {

  def term(tpe: Term.TermType.EnumVal, args: Seq[Term], optargs: Seq[Term.AssocPair] = Nil) = {
    Term(
      `type` = Some(tpe),
      `args` = args,
      `optargs` = optargs)
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

  def makeArrayTerm(name: String) = {
    term(
      tpe = Term.TermType.MAKE_ARRAY,
      args = Seq(
      )
    )
  }

  def strDatum(value: String) = {
    Datum(
      `type` = Some(Datum.DatumType.R_STR),
      `rStr` = Some(value)
    )
  }

  def intDatum(value: Int) = {
    Datum(`type` = Some(Datum.DatumType.R_NUM))
  }

  def startQuery(token: Long, query: Term) = {
    Query(
      `token` = Some(token),
      `type` = Some(Query.QueryType.START),
      `query` = Some(query)
    )
  }

}
