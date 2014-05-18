package srethink.core.ast

import java.util.Date


trait RValue

trait Expr  extends RValue

trait Func extends RValue

case class RField(val path: Seq[String])
    extends RValue with RDynamic {

  def === (value: Any) = FieldEq(path, value)

  def > (value: Any) = FieldGt(path, value)

  def < (value: Any) = FieldLt(path, value)

  def matches(pattern: String) = FieldMatch(path, pattern)

  def during(from: Date, to: Date) = FieldDuring(path, from, to)
}

case class RDatabase(name: String) extends Dynamic with RValue {
  val tables =  collection.mutable.Map[String, RTable]()
  def selectDynamic(name: String) =
    tables.getOrElseUpdate(name, new RTable(name))
}

case class RTable(name: String) extends RValue with RDynamic {
  val path = Nil
}

trait RDynamic extends Dynamic {
  val path: Seq[String]
  val fields =  collection.mutable.Map[String, RField]()
  def selectDynamic(name: String) =
    fields.getOrElseUpdate(name, new RField(path :+ name))
}
