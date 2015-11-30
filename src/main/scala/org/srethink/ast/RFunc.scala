package org.srethink.ast

import io.circe._
import org.srethink.ast.Helper._
import org.srethink.net._

case class RVar(id: Int) extends Ast {
  def toTerm = term(TermConstant.VAR_VALUE, Seq(Json.int(id)), Nil)
}
