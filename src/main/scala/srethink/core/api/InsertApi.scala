package srethink.core

trait InsertApi {

  this: JsonTypes =>

  case class InsertResult(
    deleted: Int,
    errors: Int,
    generatedKeys: Seq[String],
    inserted: Int,
    replaed: Int,
    skipped: Int,
    unchange: Int)

  case class InsertOptions(
    durability: String,
    returnVals: Boolean,
    upsert: Boolean)

  case class Insert(
    table: String,
    data: JsonArray,
    options: Option[InsertOptions] = None
  ) extends Api[InsertResult]

  implicit object InsertEncoder extends QueryEncoder[Insert] {
    import CodecHelper._
    def encode(token: Long, api: Insert) = {
      ???
    }
  }
}
