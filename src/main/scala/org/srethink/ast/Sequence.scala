package org.srethink.ast

import io.circe._
import org.srethink.net._

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
}

object Sequence {
  def apply(term: Json): Sequence = Selection(term)
  private case class Selection(term: Json) extends Sequence
}
