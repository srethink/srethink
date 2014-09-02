package play.api.rql

import play.api.libs.json
import play.api.libs.json.{Reads, Writes, Json}
import srethink.json._
import srethink.ast._

trait PlayJsonDef extends JsonDef with ResultDecoders with PlayRethinkFormats {
  type JsValue = json.JsValue
  type JsBool = json.JsBoolean
  type JsNumber = json.JsNumber
  type JsString = json.JsString
  type JsArray = json.JsArray
  type JsObject = json.JsObject
  type JsEncoder[T] = json.Writes[T]
  type JsDecoder[T] = json.Reads[T]

  def jsArray(items: Seq[JsValue]) = new JsArray(items)
  def jsBool(value: Boolean) = new JsBool(value)
  def jsNumber(value: Long) = new JsNumber(value)
  def jsNumber(value: Double) = new JsNumber(value)
  def jsString(value: String) = new JsString(value)
  def jsObject(fields: Seq[(String, JsValue)]) = new JsObject(fields)

  def unapplyJsObject(obj: JsObject) = obj.fields
  def unapplyJsArray(arr: JsArray) = arr.value

  def stringify(js: JsValue) = Json.stringify(js)
  def parse(js: String) = Json.parse(js)
  def encode[T: JsEncoder](t: T) = Json.toJson(t)
  def decode[T: JsDecoder](js: JsValue) = Json.fromJson[T](js).get

  val insertRJsDecoder = Json.reads[InsertResult]
  val createRJsDecoder = Json.reads[CreateResult]
  val dropRJsDecoder = Json.reads[DropResult]
  val deleteRJsDecoder = Json.reads[DeleteResult]
}
