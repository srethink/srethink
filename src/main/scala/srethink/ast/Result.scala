package srethink.ast

class RethinkException(msg: String) extends Exception(msg)

case class InsertResult(
  inserted: Int,
  replaced: Int,
  unchanged: Int,
  errors: Int,
  deleted: Int,
  skipped: Int,
  first_error: Option[String],
  generated_keys: Seq[Either[String, Double]])

case class CreateResult(created: Int)
case class DropResult(dropped: Int)

trait ResultDecoders { this: srethink.json.JsonDef =>
  implicit val insertRJsDecoder: JsDecoder[InsertResult]
  implicit val createRJsDecoder: JsDecoder[CreateResult]
  implicit val dropRJsDecoder: JsDecoder[DropResult]
}
