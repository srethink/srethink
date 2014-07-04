package srethink.api

import scala.concurrent.Future
import srethink.ast._
import srethink.net._
import srethink.protocol._
import srethink.protocol.Response.ResponseType

private[api] trait ConnectionAware {
  val connection: Connection
}

private[api] trait TokenGenerator {
  def nextToken: Long = TokenGenerator.gen.incrementAndGet
}

object TokenGenerator {
  val gen = new java.util.concurrent.atomic.AtomicLong
}

private[api] trait TermQuery
    extends ConnectionAware
    with TokenGenerator {

  implicit val ctx = connection.config.executionContext

  def query(term: RTerm) = {
    val q = Query(
      `type` = Some(Query.QueryType.START),
      query = Some(term.toTerm),
      token = Some(nextToken)
    )
    connection.query(q)
  }

  def singleSelect[T](term: RTerm, decoder: DatumDecoder[T]): Future[Option[T]] = {
    nomalize(query(term)).map {
      case QuerySuccess(_, data) => data.headOption.map(decoder.decode)
    }
  }

  def sequenceSelect[T](term: RTerm, decoder: DatumDecoder[T]): Future[Seq[T]] = {
    nomalize(query(term)).map {
      case QuerySuccess(_, data) => data.map(decoder.decode)
    }
  }

  private def nomalize(resp: Future[Response]) = {
    import ResponseType._
    for(r <- resp) yield {
      r.`type`.get match {
        case SUCCESS_ATOM | SUCCESS_PARTIAL | SUCCESS_SEQUENCE =>
          QuerySuccess(r.`type`.get, r.response.to[Seq])
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
