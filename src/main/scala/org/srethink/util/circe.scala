package org.srethink.util

import io.circe._
import cats.free.Trampoline
import cats.std.function._
import cats.std.list._
import cats.syntax.traverse._
import org.srethink.ast._
import scala.util.Try

object circe {
  implicit class CirceSyntax(val json: Json) extends AnyVal {

    private def transform(j: Json, f: PartialFunction[Json, Json]): Trampoline[Json] = {
      if(f.isDefinedAt(j)) {
        Trampoline.done(f(j))
      } else {
        j.arrayOrObject(
          Trampoline.done(j) ,
          _.traverse(e => Trampoline.suspend(transform(e, f))).map(Json.fromValues),
          _.traverse(e => Trampoline.suspend(transform(e, f))).map(Json.fromJsonObject)
        )
      }
    }

    def partialMap(f: PartialFunction[Json, Json]): Json = transform(json, f).run

    def encodeDates(format: String, timezone: String) = partialMap {
      case j if isDate(j, format) =>
        val ts = BigDecimal(parse(j.asString.get, format).getTime) / 1000
        Json.obj(
          "$reql_type$" -> Json.fromString("TIME"),
          "epoch_time" -> Json.fromBigDecimal(ts),
          "timezone" -> Json.fromString(timezone)
        )
    }

    def encodeArray(): Json = {
      def enc(j: Json): Trampoline[Json] = j.arrayOrObject(
        Trampoline.done(j),
        _.traverse(e => Trampoline.suspend(enc(e))).map(Helper.makeArray),
        _.traverse(e => Trampoline.suspend(enc(e))).map(Json.fromJsonObject)
      )
      enc(json).run
    }


    def decodeDates(format: String): Json = partialMap {
      case j if isReqlDate(j) =>
        val ts = (j.asObject.get("epoch_time").get.asNumber.get.toDouble * 1000).toLong
        val dateStr = formatDate(new java.util.Date(ts), format)
        Json.fromString(dateStr)
    }

    private def isReqlDate(j: Json) = {
      j.asObject.fold(false)(obj => obj("$reql_type$") == Some(Json.fromString("TIME")))
    }

    private def isDate(j: Json, fmt: String) = {
      j.asString.fold(false)(isDateStr(_, fmt))
    }

    private def isDateStr(str: String, fmt: String) = {
      Try(parse(str, fmt)).isSuccess
    }

    private def formatDate(d: java.util.Date, fmt: String) = {
      val sdf = new java.text.SimpleDateFormat(fmt)
      sdf.format(d)
    }

    private def parse(str: String, fmt: String) =  {
      val sdf = new java.text.SimpleDateFormat(fmt)
      sdf.parse(str)
    }
  }
}
