package org.srethink.ast

import io.circe.Json

trait Ast {
  def toTerm: Json
}
