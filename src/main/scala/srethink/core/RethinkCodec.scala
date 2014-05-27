package srethink.core

import scala.collection.immutable._
import srethink.protocol._

trait DatumDecoder[+T] {
  def decode(data: Datum): T
}

trait DatumEncoder[-T] {
  def encode(t: T): Datum
}

trait QueryEncoder[T] {
  def encode(token: Long, api: T): Query
}

trait ResponseDecoder[T] {
  def decode(response: Response): Either[QueryError, T]
}
