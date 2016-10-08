package org.srethink.ast

import io.circe._

trait Datum[V] extends Ast with DocLike

object Datum {
  def as[T](t: Json) = new Datum[T] {
    def term = t
  }
}
