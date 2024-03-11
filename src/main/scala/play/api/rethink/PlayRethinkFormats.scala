package play.api.rethink

import java.util.Date
import play.api.libs.json._
import play.api.libs.json.{JsValue => JS}
import srethink.ast._
import srethink.protocol.TermConstant.MAKE_ARRAY_VALUE

trait PlayRethinkFormats {

  implicit def eitherReads[A: Reads, B: Reads]: Reads[Either[A, B]] = {
    new Reads[Either[A, B]] {
      def reads(json: JS) = {
        Json.fromJson[A](json).map(Left[A, B](_)).orElse {
          Json.fromJson[B](json).map(Right[A, B](_))
        }
      }
    }
  }

  implicit def eitherWrites[A: Writes, B: Writes]: Writes[Either[A, B]] = {
    new Writes[Either[A, B]] {
      def writes(e: Either[A, B]) = {
        e match {
          case Left(a)  => Json.toJson(a)
          case Right(b) => Json.toJson(b)
        }
      }
    }
  }

  private val readRqlTime: (String, Double) => JsResult[Date] = {
    case (rqlType, epochTime) =>
      if (rqlType != "TIME") {
        JsError("error.expected.rql_type to be TIME")
      } else {
        JsSuccess(new Date((epochTime * 1000).toLong))
      }
  }

  implicit val timeReads: Reads[Date] = new Reads[Date] {
    def reads(json: JS) = {
      json match {
        case JsObject(props)
            if props.get("$reql_type$") == Some(JsString("TIME")) =>
          val JsNumber(t) = props("epoch_time")
          JsSuccess(new Date((t * 1000).toLong))
      }
    }
  }

  implicit val timeWrites: Writes[Date] = new Writes[Date] {
    def writes(date: Date) = {
      JsObject(
        Seq(
          "$reql_type$" -> JsString("TIME"),
          "epoch_time"  -> JsNumber(BigDecimal(date.getTime) / 1000),
          "timezone"    -> JsString("+00:00")
        )
      )
    }
  }

  lazy implicit val insertRFormat: Format[InsertResult] =
    Json.format[InsertResult]
  lazy implicit val createRFormat: Format[CreateResult] =
    Json.format[CreateResult]
  lazy implicit val dropRFormat: Format[DropResult] = Json.format[DropResult]
  lazy implicit val deleteRFormat: Format[DeleteResult] =
    Json.format[DeleteResult]
  lazy implicit val updateRFormat: Format[UpdateResult] =
    Json.format[UpdateResult]
  val booleanF: Format[Boolean] = implicitly[Format[Boolean]]
  val intF: Format[Int]         = implicitly[Format[Int]]
  val longF: Format[Long]       = implicitly[Format[Long]]
  val floatF: Format[Float]     = implicitly[Format[Float]]
  val doubleF: Format[Double]   = implicitly[Format[Double]]
  val stringF: Format[String]   = implicitly[Format[String]]
  val dateF: Format[Date]       = implicitly[Format[Date]]
}