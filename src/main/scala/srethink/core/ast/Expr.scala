package srethink.core.ast

import java.util.Date


trait HasOperators extends Expr {

  def + (that: HasOperators) = Add(this, that)

  def - (that: HasOperators) = Minus(this, that)

  def * (that: HasOperators) = Multiply(this, that)

  def / (that: HasOperators) = Divide(this, that)

  def === (that: HasOperators) = Equals(this, that)

  def =!= (that: HasOperators) = NotEquals(this, that)
}

trait HasBiOperators extends Expr {
  def and(that: HasBiOperators) = And(this, that)
  def or(that: HasBiOperators) = Or(this, that)
  def &&(that: HasBiOperators) = and(that)
  def ||(that: HasBiOperators) = or(that)
}

case class And(left: HasBiOperators, right: HasBiOperators) extends HasBiOperators

case class Or(left: HasBiOperators, right: HasBiOperators) extends HasBiOperators

case class Eq(left: Expr, right: Expr) extends HasBiOperators

case class NotEq(left: Expr, right: Expr) extends HasBiOperators

case class Add(left: HasOperators, right: HasOperators) extends HasOperators

case class Minus(left: HasOperators, right: HasOperators) extends  HasOperators

case class Multiply(left: HasOperators, right: HasOperators) extends  HasOperators

case class Divide(left: HasOperators, right: HasOperators) extends  HasOperators

case class Equals(left: HasOperators, right: HasOperators) extends  HasOperators

case class NotEquals(left: HasOperators, right: HasOperators) extends HasOperators
