package srethink.ast

import srethink.protocol._

object DatumHelper {
  import Datum.DatumType._
  def strDatum(value: String) = {
    Datum(Some(R_STR), rStr = Some(value))
  }
}

object TermHelper {

  import Term.TermType._
  import DatumHelper._

  def datumTerm(value: Datum) = {
    Term(
      `type` = Some(DATUM),
      datum = Some(value)
    )
  }

  def strTerm(value: String) = {
    datumTerm(strDatum(value))
  }
}
