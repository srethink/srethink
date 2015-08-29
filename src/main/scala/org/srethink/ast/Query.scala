package org.srethink.ast

import org.srethink.net._

case class RQuery(
  queryType: Int,
  query: RTerm,
  options: Seq[_ <: ROption])

trait RTerm extends RQLType {
  val termType: Int
  val args: Seq[RTerm]
  val optArgs: Seq[_ <: ROption]
}
trait ROption
sealed trait RQLType
trait RSequence extends RTerm
trait RArray extends RSequence
trait RStream extends RSequence
trait RStreamSelection extends RStream
trait RObject extends RTerm
trait RSingleSelection extends RObject
class RDatabase(val name: String) extends AnyVal
trait RDatum extends RTerm

case class RTable(name: String, optArgs: Seq[_ <: ROption]) extends  RStreamSelection {
  val termType = TermConstant.TABLE_VALUE
  val args = Seq()
}

case class RGetAll(table: RTable) extends RArray
