package srethink.core


sealed trait RethinkResponse

case class QuerySuccess[T](t: T) extends RethinkResponse

case class QueryFailure(message: String, backtrace: String)
    extends RethinkResponse
