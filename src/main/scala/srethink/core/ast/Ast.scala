package srethink.core.ast

import java.util.Date

object r  {

}


trait Conds {
  trait Cond
  trait GeneralOps extends Cond {

    val DefaultTrue = 1
    val DefaultError = 2

    case class Default(cond: Cond, defaultType: Int) extends Cond

    def defaultTrue = Default(this, DefaultTrue)

    def defaultError = Default(this, DefaultError)

    def and(that: Cond) = And(this, that)

    def or(that: Cond) = Or(this, that)

    def &&(that: Cond) = and(that)

    def ||(that: Cond) = and(that)

  }

  case class FieldEq(field: Seq[String], value: Any) extends Cond
  case class FieldGt(field: Seq[String], value: Any) extends Cond
  case class FieldLt(field: Seq[String], value: Any) extends Cond
  case class FieldDuring(field: Seq[String], from: Date, to: Date) extends Cond
  case class FieldMatch(field: Seq[String], pattern: String) extends Cond
  case class And(a: Cond, b: Cond)
  case class Or(a: Cond, b: Cond)
}

private[ast] trait Asts extends Conds {

  trait RValue

  trait RDynamic extends Dynamic {
    val path: Seq[String]
    val fields =  collection.mutable.Map[String, RField]()
    def selectDynamic(name: String) = fields.getOrElseUpdate(name, new RField(path :+ name))
  }

  class RField(val path: Seq[String]) extends RValue with RDynamic {

    def === (value: Any) = FieldEq(path, value)

    def > (value: Any) = FieldGt(path, value)

    def < (value: Any) = FieldLt(path, value)

    def matches(pattern: String) = FieldMatch(path, pattern)

    def during(from: Date, to: Date) = FieldDuring(path, from, to)
  }

  class RTable(name: String) extends RValue with RDynamic {
    val path = Nil
  }
}

trait FilterApi {
}
