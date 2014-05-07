package srethink.core.codec

import scala.language.experimental.macros
import srethink.core._
import srethink.protocol._

object Codecs extends  BaseDatumEncoders with BaseDatumDecoders with AdditionalEncoders {

  def encoder[T]: DatumEncoder[T] = macro CodecMacro.encoderImpl[T]

  def decoder[T]: DatumDecoder[T] = macro CodecMacro.decoderImpl[T]

  def encode[T: DatumEncoder](t: T): Datum = implicitly[DatumEncoder[T]].encode(t)

  def decode[T: DatumDecoder](datum: Datum) = implicitly[DatumDecoder[T]].decode(datum)
}
