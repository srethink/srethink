package srethink.api

import java.util.Date
import srethink.protocol._
import srethink.ast._
import Datum.DatumType._

class CodecException(val field: String, tpe: String = "", value: Any = "" )
    extends Exception(s"Cannot encode/deconde field ${field}, value: ${value}, expect type: ${tpe}")

trait RDecoder[+T] {
  def decode(datum: Datum): Option[T]
}

trait REncoder[-T] {
  def encode(t: T): RDatum
}

trait BasicEncoders {

  implicit object  IntEncoder extends REncoder[Int] with RDecoder[Int] {
    def encode(t: Int) = new RNum(t)
    def decode(t: Datum) = {
      for {
        tp <- t.`type` if tp == R_NUM
        v <- t.rNum
      } yield v.toInt
    }

  }

  implicit object  LongEncoder extends REncoder[Long] {
    def encode(t: Long) = new RNum(t)
    def decode(t: Datum) = {
      for {
        tp <- t.`type` if tp == R_NUM
        v <- t.rNum
      } yield v.toLong
    }
  }

  implicit object FloatEncoder extends REncoder[Float] {
    def encode(t: Float) = new RNum(t)

    def decode(t: Datum) = {
      for {
        tp <- t.`type` if tp == R_NUM
        v <- t.rNum
      } yield v.toFloat
    }
  }

  implicit object DoubleEncoder extends REncoder[Double] {
    def encode(t: Double) = new RNum(t)
    def decode(t: Datum) = {
      for {
        tp <- t.`type` if tp == R_NUM
        v <- t.rNum
      } yield v
    }
  }

  implicit object StrEncoder extends REncoder[String] {
    def encode(t: String) = new RStr(t)
    def decode(t: Datum) = {
      for {
        tp <- t.`type` if tp == R_NUM
        v <- t.rStr
      } yield v
    }
  }

  implicit object DateEncoder extends REncoder[Date] {
    def encode(t: Date) = new RObject(
      Seq(
        "$reql_type$" -> new RStr("TIME"),
        "epoch_time" -> new RNum(t.getTime / 1000.00),
        "timezone" -> new RStr("+00:00")
      )
    )

    def decode(t: Datum) = {
      for {
        tp <- t.`type` if tp == R_OBJECT
        v <- t.rNum
      } yield v.toInt
    }
  }
}
