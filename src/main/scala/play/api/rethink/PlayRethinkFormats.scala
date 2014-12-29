package play.api.rethink

import java.util.Date
import play.api.libs.json._
import play.api.libs.json.{JsValue => JS}
import play.api.data.validation.ValidationError
import srethink.ast._

trait PlayRethinkFormats {

  implicit def eitherReads[A: Reads, B: Reads]: Reads[Either[A, B]] =  {
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
          case Left(a) => Json.toJson(a)
          case Right(b) => Json.toJson(b)
        }
      }
    }
  }

  private val readRqlTime : (String, Double) => JsResult[Date] = {
    case (rqlType, epochTime) =>
      if(rqlType != "TIME") {
        JsError(Seq(JsPath() -> Seq(ValidationError("error.expected.rql_type to be TIME"))))
      } else {
        JsSuccess(new Date((epochTime * 1000).toLong))
      }
  }

  implicit val timeReads : Reads[Date] = new Reads[Date] {
    def reads(json: JS) = {
      json match {
        case JsObject(("$reql_type$", JsString("TIME")) +: ("epoch_time", JsNumber(t)) +: tail) =>
          JsSuccess(new Date((t * 1000).toLong))
      }
    }
  }

  implicit val timeWrites : Writes[Date] = new Writes[Date] {
    def writes(date: Date) = {
      JsObject(
        Seq(
          "$reql_type$" -> JsString("TIME"),
          "epoch_time" -> JsNumber(BigDecimal(date.getTime) / 1000),
          "timezone" -> JsString("+00:00")
        )
      )
    }
  }

  implicit val insertRFormat = Json.format[InsertResult]
  implicit val createRFormat = Json.format[CreateResult]
  implicit val dropRFormat = Json.format[DropResult]
  implicit val deleteRFormat = Json.format[DeleteResult]
}
