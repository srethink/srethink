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
  generated_keys: Option[Seq[Either[String, Double]]])

case class CreateResult(created: Int)
case class DropResult(dropped: Int)
case class DeleteResult(
  deleted: Int,
  skipped: Int,
  errors: Int,
  first_error: Option[String])

trait ResultDecoders { this: srethink.json.JsonDef =>
  implicit val insertRJsDecoder: JsDecoder[InsertResult]
  implicit val createRJsDecoder: JsDecoder[CreateResult]
  implicit val dropRJsDecoder: JsDecoder[DropResult]
  implicit val deleteRJsDecoder: JsDecoder[DeleteResult]
}
