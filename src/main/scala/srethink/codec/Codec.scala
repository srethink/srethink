package srethink.codec

import srethink.protocol._

trait Decoder[T] {
  def decode(data: Datum): T
}

trait Encoder[T] {
  def encode(t: T): Datum
}

trait Codec[T] extends Decoder with Encoder

trait JsonStringDecoder extends Decoder[String] {

  def decode(data: Datum) = {
    require(data.`type` == Datum.DatumType.EnumVal.R_JSON)
    data.rStr
  }

}

trait JsonStringEncoder extends Decoder[String] {

  def encode(t: String) = {
    Datum(
      `type` = Datum.DatumType.EnumVal.R_JSON,
      rStr = Some(t)
    )
  }
}

object Codecs {
  implicit object JsonStringCodec extends JsonStringDecoder with JsonStringEncoder
}


object Codecs {

}
