package srethink.codec

import srethink.protocol._

trait Decoder[T] {
  def decode(data: Datum): T
}

trait Encoder[T] {
  def encode(t: T): Datum
}

trait Codec[T] extends Decoder[T] with Encoder[T]

trait JsonStringDecoder extends Decoder[String] {

  def decode(data: Datum) = {
    require(data.`type` == Some(Datum.DatumType.R_JSON))
    data.rStr.get
  }
}

trait JsonStringEncoder extends Decoder[String] {

  def encode(t: String) = {
    Datum(
      `type` = Some(Datum.DatumType.R_JSON),
      rStr = Some(t)
    )
  }
}

object Codecs {
  implicit object JsonStringCodec extends JsonStringDecoder with JsonStringEncoder
}
