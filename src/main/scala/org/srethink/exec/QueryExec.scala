package org.srethink.exec

import cats.data.Xor
import cats.syntax.traverse._
import io.circe._
import cats.std.list._
import io.circe.parser._
import io.circe.generic.auto._
import java.util.concurrent.atomic.AtomicLong
import org.srethink.ast._
import org.srethink.net._
import org.srethink.util.circe._

import scala.concurrent.Future

case class QueryResult(t: Int, r: List[Json], b: Option[String])

case class ExecConfig(
  connectionFactory: ConnectionFactory,
  dateTimeFormat: String = "yyyy-MM-dd HH:mm:ss",
  timezone: String)

class QueryExec(val config: ExecConfig) {
  val tg = new AtomicLong(0)
  implicit val ec = trampoline

  def execute(json: Json) = {
    val m = Message(tg.incrementAndGet(), json.toString)
    config.connectionFactory.get().flatMap(_.execute(m).map(_.body))
  }

  def query(json: Json) = {
    execute(Helper.startQuery(json))
  }

  def run[T: Decoder](ast: Sequence) = query(ast.term).flatMap(decodeAsFuture[T])

  def run[T: Decoder](ast: Atom) = query(ast.term).flatMap(decodeAsFuture[T]).map(_.head)

  def run(action: Action) = query(action.term).flatMap(decodeAsFuture[Json])

  private def decodeAsFuture[T: Decoder](body: String) = {
    val decodeR = for {
      j <- parse(body)
      decoded = j.decodeDates(config.dateTimeFormat)
      rs <- implicitly[Decoder[QueryResult]].decodeJson(decoded)
      r <- decodeResult(rs)
    } yield r
    decodeR.fold(e => Future.failed(new Exception(s"Response: $body" ,e)), Future.successful(_))
  }

  private def decodeResult[T: Decoder](r: QueryResult) = {
    if(r.t == ResponseType.SUCCESS_ATOM
      || r.t == ResponseType.SUCCESS_SEQUENCE
      || r.t == ResponseType.SUCCESS_SEQUENCE) {
      r.r.traverse(implicitly[Decoder[T]].decodeJson(_))
    } else {
      Xor.Left(new Exception(r.b.getOrElse("unknow error")))
    }
  }
}
