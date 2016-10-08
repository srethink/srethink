package org.srethink.ast

import io.circe._
import org.srethink.net._

case class Var(i: Int) extends Dynamic with DocLike {
  def term = Helper.term(TermType.VAR, Seq(Json.fromInt(i)))
}

case class Func(argc: Int, body: Ast) extends Ast {
  def term = {
    val vars = Helper.makeArray((1 to argc).map(Json.fromInt))
    Helper.term(TermType.FUNC, Seq(vars, body.term))
  }
}

object Func {
  def apply(f: Var => Ast): Func = Func(1, f(Var(1)))
  def apply(f: (Var, Var) => Ast): Func = Func(2, f(Var(1), Var(2)))
}
