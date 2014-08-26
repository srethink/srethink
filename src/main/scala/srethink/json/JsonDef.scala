package srethink.json

/**
 * This trait aims to support multi json serialize library
 */
trait JsonTypes {
  type JsValue
  type JsBool <: JsValue
  type JsNumber <: JsValue
  type JsString <: JsValue
  type JsArray <: JsValue
  type JsObject <: JsValue
  type JsEncoder[T]
  type JsDecoder[T]
}

trait JsonMethods { this: JsonTypes =>
  implicit def jsBool(value: Boolean): JsBool
  implicit def jsNumber(value: Double): JsNumber
  implicit def jsNumber(value: Long): JsNumber
  implicit def jsString(value: String): JsString
  implicit def jsArray(item: Seq[JsValue]): JsArray
  implicit def jsObject(fields: Seq[(String, JsValue)]): JsObject

  def unapplyJsObject(obj: JsObject): Seq[(String, JsValue)]
  def unapplyJsArray(arr: JsArray): Seq[JsValue]

  def stringify(json: JsValue): String
  def parse(json: String): JsValue
  def encode[T: JsEncoder](t: T): JsValue
  def decode[T: JsDecoder](js: JsValue): T
}

trait JsonDef extends JsonTypes with JsonMethods
