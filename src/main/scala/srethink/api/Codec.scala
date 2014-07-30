package srethink.api

import java.util.Date
import srethink.protocol._
import srethink.api._
import Datum.DatumType._

class CodecException(val field: String, tpe: String = "", value: Any = "" )
    extends Exception(s"Cannot encode/deconde field ${field}, value: ${value}, expect type: ${tpe}")

trait RDecoder[+T] {
  def decode(datum: Option[Datum]): Option[T]
}

trait REncoder[-T] {
  def encode(t: T): RDatum
}

trait BasicCodecs {

  implicit object BooleanEncoder extends REncoder[Boolean] with RDecoder[Boolean] {
    def encode(t: Boolean) = new RBool(t)
    def decode(t: Option[Datum])= {
      for {
        d <- t
        tp <- d.`type` if tp == R_BOOL
        v <- d.rBool
      } yield v
    }
  }

  implicit object  IntEncoder extends REncoder[Int] with RDecoder[Int] {
    def encode(t: Int) = new RNum(t)
    def decode(t: Option[Datum]) = {
      for {
        d <- t
        tp <- d.`type` if tp == R_NUM
        v <- d.rNum
      } yield v.toInt
    }
  }

  implicit object  LongEncoder extends REncoder[Long] with RDecoder[Long] {
    def encode(t: Long) = new RNum(t)
    def decode(t: Option[Datum]) = {
      for {
        d <- t
        tp <- d.`type` if tp == R_NUM
        v <- d.rNum
      } yield v.toLong
    }
  }

  implicit object FloatEncoder extends REncoder[Float] with RDecoder[Float] {
    def encode(t: Float) = new RNum(t)

    def decode(t: Option[Datum]) = {
      for {
        d <- t
        tp <- d.`type` if tp == R_NUM
        v <- d.rNum
      } yield v.toFloat
    }
  }

  implicit object DoubleEncoder extends REncoder[Double] with RDecoder[Double] {
    def encode(t: Double) = new RNum(t)
    def decode(t: Option[Datum]) = {
      for {
        d <- t
        tp <- d.`type` if tp == R_NUM
        v <- d.rNum
      } yield v
    }
  }

  implicit object StrEncoder extends REncoder[String] with RDecoder[String] {

    def encode(t: String) = new RStr(t)

    def decode(t: Option[Datum]) = {
      for {
        d <- t
        tp <- d.`type` if tp == R_STR
        v <- d.rStr
      } yield v
    }
  }

  implicit object DateEncoder extends REncoder[Date] with RDecoder[Date] {
    def encode(t: Date) = new RObject(
      Seq(
        "$reql_type$" -> new RStr("TIME"),
        "epoch_time" -> new RNum(t.getTime / 1000.00),
        "timezone" -> new RStr("+00:00")
      )
    )

    def decode(t: Option[Datum]) = {
      for {
        d <- t
        tp <- d.`type` if tp == R_OBJECT
        v <- d.rObject.collectFirst {
          case Datum.AssocPair(Some("epoch_time"), Some(time)) => time
        }
        epochTime <- v.rNum
      } yield new Date((epochTime * 1000).toLong)
    }
  }
}

trait AdditionalCodec {
  implicit def optionEncoder[T: REncoder] = {
    new REncoder[Option[T]] {
      val encoder = implicitly[REncoder[T]]
      def encode(t: Option[T]) = {
        t.map(encoder.encode).getOrElse(RNull)
      }
    }
  }

  implicit def traversableEncoder[T: REncoder] = {
    val encoder = implicitly[REncoder[T]]
    new REncoder[Traversable[T]] {
      def encode(t: Traversable[T]) = {
        new RArray(t.map(encoder.encode).toSeq)
      }
    }
  }

  implicit def optionDecoder[T: RDecoder]: RDecoder[Option[T]] = {
    new RDecoder[Option[T]] {
      val decoder = implicitly[RDecoder[T]]
      def decode(t: Option[Datum]) = {
        Some(decoder.decode(t))
      }
    }
  }

  implicit def traversableDecoder[F[_], A](implicit bf: collection.generic.CanBuildFrom[F[_], A, F[A]], decoder: RDecoder[A]): RDecoder[F[A]] = {
    new RDecoder[F[A]] {
      def decode(t: Option[Datum]) = {
        for {
          d <- t
          tp <- d.`type` if tp  == R_ARRAY
        } yield {
          val builder = bf()
          d.rArray.foreach { e =>
            builder += (decoder.decode(Some(e))).get
          }
          builder.result()
        }
      }
    }
  }

  implicit def seqDecoder[A: RDecoder] = traversableDecoder[Seq, A]
  implicit def setDecoder[A: RDecoder] = traversableDecoder[Set, A]
}
