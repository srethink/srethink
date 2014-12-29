package play.api.rethink

import java.util.Date
import play.api.libs.json._
import play.api.libs.json.Json
import srethink.json._
import srethink.ast._
import srethink._

trait PlayRQL extends AstDef[JsValue, Format] with PlayRethinkFormats {

  def jsArray(items: Seq[JsValue]) = new JsArray(items)
  def jsObject(fields: Seq[(String, JsValue)]) = new JsObject(fields)

  def unapplyJsObject(obj: JsValue) = {
    val JsObject(fields) = obj
    fields
  }
  def unapplyJsArray(arr: JsValue): Seq[JsValue] = {
    val JsArray(ems) = arr
    ems
  }

  def stringify(js: JsValue) = Json.stringify(js)
  def parse(js: String) = Json.parse(js)
  def encode[T: Format](t: T) = Json.toJson(t)
  def decode[T: Format](json: JsValue) = Json.fromJson[T](json).get

  lazy implicit val booleanF = implicitly[Format[Boolean]]
  lazy implicit val intF = implicitly[Format[Int]]
  lazy implicit val longF = implicitly[Format[Long]]
  lazy implicit val floatF = implicitly[Format[Float]]
  lazy implicit val doubleF = implicitly[Format[Double]]
  lazy implicit val stringF = implicitly[Format[String]]
}
