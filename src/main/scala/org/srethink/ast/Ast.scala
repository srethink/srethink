package org.srethink.ast

import io.circe.Json
import org.srethink.net._
import scala.language.dynamics

trait Ast extends Any {
  def term: Json
}

trait Action extends Ast
