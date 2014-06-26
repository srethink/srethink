package srethink.api

import srethink.protocol._

trait DatumDecoder[T] {
  def decode(datum: Datum): T
}
