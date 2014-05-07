package srethink.core.codec

import scala.language.experimental.macros
import srethink.protocol._
import srethink.core._
import CodecHelper._

object Codecs extends  BaseDatumEncoders with BaseDatumDecoders {
  def encoder[T]: DatumEncoder[T] = macro CodecMacro.encoderImpl[T]
}

trait BaseDatumDecoders {

  implicit object IntDecoder extends DatumDecoder[Int] {
    def decode(datum: Datum) = {
      for {
        t <- datum.`type` if t == Datum.DatumType.R_NUM
        v <- datum.rNum
      } yield v.toInt
    }
  }

  implicit object StringDecoder extends DatumDecoder[String] {
    def decode(datum: Datum) = {
      for {
        t <- datum.`type` if t == Datum.DatumType.R_STR
        v <- datum.rStr
      } yield v
    }
  }

  implicit object LongDecoder extends DatumDecoder[Long] {
    def decode(datum: Datum) = {
      for {
        t <- datum.`type` if t == Datum.DatumType.R_NUM
        v <- datum.rNum
      } yield v.toLong
    }
  }

  implicit object BooleanDecoder extends DatumDecoder[Boolean] {
    def decode(datum: Datum) = {
      for {
        t <- datum.`type` if t == Datum.DatumType.R_BOOL
        v <- datum.rBool
      } yield v
    }
  }
}



trait BaseDatumEncoders {

  implicit object IntEncoder extends DatumEncoder[Int] {
    def encode(t: Int) = {
      numDatum(t)
    }
  }

  implicit object StringEncoder extends DatumEncoder[String] {
    def encode(t: String) = {
      strDatum(t)
    }
  }

  implicit object LongEncoder extends DatumEncoder[Long] {
    def encode(t: Long) = {
      numDatum(t)
    }
  }

  implicit object BooleanEncoder extends DatumEncoder[Boolean] {
    def encode(t: Boolean) = {
      boolDatum(t)
    }
  }

  def encode[T: DatumEncoder](t: T) = {
    implicitly[DatumEncoder[T]].encode(t)
  }

  def encodeBaseType(t: Any) = t match {
    case v: Int => encode(v)
    case v: String => encode(v)
    case v: Long => encode(v)
    case v: Boolean => encode(v)
  }
}
