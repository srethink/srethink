package org

import io.circe._

package object srethink {
  implicit def astEncoder[T <: ast.Ast]: Encoder[T] = new Encoder[T] {
    def apply(v: T) = v.term
  }
}
