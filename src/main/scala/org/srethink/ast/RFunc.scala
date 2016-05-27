package org.srethink.ast

import io.circe._
import org.srethink.ast.Helper._
import org.srethink.net._

case class RVar(id: Int) extends Ast {
  def toTerm = term(TermType.VAR, Seq(Json.int(id)), Nil)
}
