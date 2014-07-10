package srethink.api

import scala.concurrent.Future
import srethink.ast._
import srethink.net._
import srethink.protocol._
import srethink.protocol.Response.ResponseType

object TokenGenerator {
  val gen = new java.util.concurrent.atomic.AtomicLong
  def nextToken: Long = TokenGenerator.gen.incrementAndGet
}

private[srethink] trait QueryExecutor {
  val connection: Connection
  implicit lazy val ctx = connection.config.executionContext

  def query(term: RTerm) = {
    val q = Query(
      `type` = Some(Query.QueryType.START),
      query = Some(term.toTerm),
      token = Some(TokenGenerator.nextToken)
    )
    normalize(connection.query(q))
  }

  def headOption[T: RDecoder](term: RTerm): Future[Option[T]] = {
    val decoder = implicitly[RDecoder[T]]
    query(term).map {
      case QuerySuccess(_, data) => decoder.decode(data.headOption)
    }
  }

  def take[T: RDecoder](term: RTerm): Future[Seq[T]] = {
    val decoder = implicitly[RDecoder[T]]
    query(term).map {
      case QuerySuccess(_, data) => data.map(s => decoder.decode(Some(s)).get)
    }
  }

  def run(term: RTerm): Future[Boolean] = {
    query(term).map(_ => true)
  }

  private def normalize(resp: Future[Response]) = {
    import ResponseType._
    for(r <- resp) yield {
      r.`type`.get match {
        case SUCCESS_ATOM | SUCCESS_PARTIAL | SUCCESS_SEQUENCE =>
          QuerySuccess(r.`type`.get, r.response)
        case COMPILE_ERROR | RUNTIME_ERROR | _ =>
          throw new QueryError(r.`type`.toString, r.response.toString, r.backtrace.toString)
      }
    }
  }
}

case class QuerySuccess(
  successType: ResponseType.EnumVal,
  data: Seq[Datum])

case class QueryError(
  errorType: String,
  message: String,
  backtrace: String
) extends Exception(s"error type: ${errorType}, message: ${message}, backtrace: ${backtrace}") {
}
