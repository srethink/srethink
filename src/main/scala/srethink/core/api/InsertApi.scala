package srethink.core

import scala.collection.immutable._
import srethink.core.codec._

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

case class Insert[T](
  table: String,
  data: Seq[T],
  options: Option[InsertOptions] = None
) extends Api[InsertResult]

object Insert{
  implicit def insertEncoder[T: DatumEncoder] = new  QueryEncoder[Insert[T]] {
    import CodecHelper._
    def encode(token: Long, api: Insert[T]) = {
      val encoder = implicitly[DatumEncoder[T]]
      val datumTerms = api.data.map { data =>
        datumTerm(encoder.encode(data))
      }
      startQuery(token, insertTerm(api.table, datumTerms))
    }
  }
}
