package srethink.ast

import srethink.protocol._
import scala.collection.immutable.Seq

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


object AstHelper {
  def function(argc: Int)(func: Seq[Var] => RTerm) = {
    val argIdx = (0 until argc)
    val argIds = DatumTerm(RArray(argIdx.map(i => RNum(i))))
    val vars = argIdx.map(i => Var(DatumTerm(RNum(i))))
    Func(argIds, func(vars))
  }

  def function1(body: Var => RTerm) = {
    function(1) { args =>
      body(args(0))
    }
  }
}
