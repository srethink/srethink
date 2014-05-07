package srethink.core.codec

import srethink.protocol._
import srethink.core._
import CodecHelper._

trait BaseDatumDecoders {

  implicit object ByteDecoder extends DatumDecoder[Byte] {

    def decode(datum: Datum) = {
      val opt = for {
        t <- datum.`type` if t == Datum.DatumType.R_NUM
        v <- datum.rNum
      } yield v.toByte
      opt.get
    }
  }

  implicit object IntDecoder extends DatumDecoder[Int] {

    def decode(datum: Datum) = {
      val opt =  for {
        t <- datum.`type` if t == Datum.DatumType.R_NUM
        v <- datum.rNum
      } yield v.toInt
      opt.get
    }
  }

  implicit object LongDecoder extends DatumDecoder[Long] {

    def decode(datum: Datum) = {
      val opt = for {
        t <- datum.`type` if t == Datum.DatumType.R_NUM
        v <- datum.rNum
      } yield v.toLong
      opt.get
    }
  }

  implicit object StringDecoder extends DatumDecoder[String] {

    def decode(datum: Datum) = {
      val opt = for {
        t <- datum.`type` if t == Datum.DatumType.R_STR
        v <- datum.rStr
      } yield v
      opt.get
    }
  }

  implicit object BooleanDecoder extends DatumDecoder[Boolean] {
    def decode(datum: Datum) = {
      val opt = for {
        t <- datum.`type` if t == Datum.DatumType.R_BOOL
        v <- datum.rBool
      } yield v
      opt.get
    }
  }
}



trait BaseDatumEncoders {

  implicit object IntEncoder extends DatumEncoder[Int] {
    def encode(t: Int) = {
      numDatum(t)
    }
  }

  implicit object ByteEncoder extends DatumEncoder[Byte] {
    def encode(t: Byte) = {
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
}
