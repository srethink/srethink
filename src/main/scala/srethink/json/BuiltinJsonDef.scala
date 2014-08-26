package srethink.json

import java.io.Writer

trait JsValue extends Any {
  def writeTo(bf: java.io.Writer)
  override def toString() = {
    val writer = new java.io.StringWriter
    writeTo(writer)
    writer.toString
  }
}
class JsBool(val value: Boolean) extends AnyVal with JsValue {
  def writeTo(writer: Writer ) {
    writer.append(value.toString)
  }
}
class JsNumber(val value: Double) extends AnyVal with JsValue {
  def writeTo(writer: Writer ) {
    writer.append(value.toString)
  }
}
class JsString(val value: String) extends AnyVal with JsValue {
  def writeTo(writer: Writer) {
    writer.append(s""""${value.toString}"""")
  }
}
class JsArray(val items: Seq[JsValue]) extends AnyVal with JsValue {
  def writeTo(writer: Writer) {
    writer.append("[")
    items.foreach { v =>
      v.writeTo(writer)
      writer.append(",")
    }
    writer.append("]")
  }
}
class JsObject(val fields: Map[String, JsValue]) extends AnyVal with JsValue {
  def writeTo(writer: Writer) {
    writer.append("{")
    fields.foreach {
      case (k, v) =>
        writer.append(s""""${k}:"""")
        v.writeTo(writer)
        writer.append(",")
    }
    writer.append("}")
  }
}

trait BuiltInJsonDef extends JsonDef {

  type JsValue = srethink.json.JsValue
  type JsBool = srethink.json.JsBool
  type JsNumber = srethink.json.JsNumber
  type JsString = srethink.json.JsString
  type JsArray = srethink.json.JsArray
  type JsObject = srethink.json.JsObject

  def jsBool(value: Boolean) = new JsBool(value)
  def jsNumber(value: Double) = new JsNumber(value)
  def jsString(value: String) = new JsString(value)
  def jsArray(items: Seq[JsValue]) = new JsArray(items)
  def jsObject(fields: Map[String, JsValue]) = new JsObject(fields)
  def stringify(js: JsValue) = js.toString()
}
