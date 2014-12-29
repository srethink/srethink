package srethink.ast

class RethinkException(msg: String) extends Exception
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

trait Models {
  type RethinkException = srethink.ast.RethinkException
  type InsertResult = srethink.ast.InsertResult
  type CreateResult = srethink.ast.CreateResult
  type DropResult = srethink.ast.DropResult
  type DeleteResult = srethink.ast.DeleteResult
}
