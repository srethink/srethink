package org.srethink.ast

import io.circe._
import io.circe.syntax._
import org.srethink.net._

case class Order(field: String, asc: Boolean, opts: Opt*) extends Ast {
  val termType = if(asc) TermType.ASC else TermType.DESC
  def term = Helper.term(
    termType,
    Seq(Json.fromString(field)),
    opts.map(_.pair)
  )
}

trait Sequence extends Ast {
  def filter(func: Var => Datum[Boolean], opts: Opt*) = {
    val t = Helper.term(
      TermType.FILTER,
      Seq(this.term, Func(func).term),
      opts.map(_.pair))
    Sequence(t)
  }

  def map(func: Var => Ast, opts: Opt*) = {
    val t = Helper.term(
      TermType.MAP,
      Seq(this.term, Func(func).term),
      opts.map(_.pair)
    )
    Sequence(t)
  }

  def count() = {
    val t = Helper.term(TermType.COUNT, Seq(this.term))
    new Atom {
      def term = t
    }
  }

  def limit(size: Long) = {
    val t = Helper.term(TermType.LIMIT, Seq(this.term, Json.fromLong(size)))
    Sequence(t)
  }

  def skip(size: Long) = {
    val t = Helper.term(TermType.SKIP, Seq(this.term, Json.fromLong(size)))
    Sequence(t)
  }

  def update[V: Encoder](v: V) = {
    Update(this, v.asJson)
  }

  def orderBy(order: Order) = {
    val t = Helper.term(
      TermType.ORDER_BY,
      Seq(this.term, order.term)
    )
    Sequence(t)
  }
}

object Sequence {
  def apply(term: Json): Sequence = Selection(term)
  private case class Selection(term: Json) extends Sequence
}
