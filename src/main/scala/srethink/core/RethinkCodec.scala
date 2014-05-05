package srethink.core

import srethink.protocol._
import scala.collection.immutable._

trait DatumDecoder[T] {
  def decode(data: Datum): Option[T]
}

trait DatumEncoder[T] {
  def encode(t: T): Datum
}

trait QueryEncoder[T] {
  def encode(token: Long, api: T): Query
}
