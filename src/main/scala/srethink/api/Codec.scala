package srethink.api

import srethink.protocol._
import srethink.ast._

trait DatumDecoder[T] {
  def decode(datum: Datum): T
}

trait ObjectEncoder[T] {
  def encode(t: T): RObject
}
