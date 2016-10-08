package org.srethink.ast

import io.circe.Json

trait Ast extends Any {
  def term: Json
}
trait Action extends Ast
trait Atom extends Ast
