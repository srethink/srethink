package org.srethink.ast

import org.srethink.net._

//RQL top level type
sealed trait RTop

trait RTerm extends RTop {
  val termType: Int
  val args: Seq[RTop]
  val optArgs: Seq[_ <: ROption]
}

trait RSequence extends RTerm
trait RArray extends RSequence
trait RStream extends RSequence
trait RStreamSelection extends RStream
trait RObject extends RTerm
trait RSingleSelection extends RObject
class RDatabase(val name: String) extends AnyVal

sealed trait RDatum extends RTop

case class RString(value: String) extends RDatum

case class RTable(name: String, optArgs: Seq[_ <: ROption]) extends  RStreamSelection {
  val termType = TermConstant.TABLE_VALUE
  val args = Seq(RString(name))
}

case class RGetAll(table: RTable, keys: Seq[RDatum], optArgs: Seq[_ <: ROption]) extends RArray {
  val termType = TermConstant.GET_ALL_VALUE
  val args = table :: keys.toList
}

case class Get(table: RTable, key: RDatum, optArgs: Seq[_ <: ROption]) extends RSingleSelection {
  val termType = TermConstant.GET_VALUE
  val args = Seq(table, key)
}
