package srethink.core.ast

import java.util.Date

trait Cond extends Expr

trait GenericCond extends Cond {
  val DefaultTrue = 1
  val DefaultError = 2

  case class Default(cond: Cond, defaultType: Int)
      extends Cond

  def defaultTrue = Default(this, DefaultTrue)
  def defaultError = Default(this, DefaultError)
  def and(that: Cond) = And(this, that)
  def or(that: Cond) = Or(this, that)
  def ? = defaultTrue
  def ! = defaultError
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
