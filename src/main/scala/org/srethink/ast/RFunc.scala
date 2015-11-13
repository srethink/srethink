package org.srethink.ast

import io.circe._

case class RVar(id: Int) extends Ast {
  def toTerm = Json.array()
}
