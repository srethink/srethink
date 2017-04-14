package org.srethink.exec

import cats.syntax.either._
import cats.syntax.traverse._
import io.circe._
import cats.instances.vector._
import cats.instances.either._
import io.circe.parser._
import io.circe.generic.auto._
import java.util.concurrent.atomic.AtomicLong
import org.srethink.ast._
import org.srethink.net._
import org.srethink.util.circe._

import scala.concurrent.Future

case class QueryResult(t: Int, r: Vector[Json], b: Option[String])

case class ExecConfig(
  connectionFactory: ConnectionFactory,
  dateTimeFormat: String = "yyyy-MM-dd HH:mm:ss",
  timezone: String)

class QueryExec(val config: ExecConfig) {
  val tg = new AtomicLong(0)
  implicit val ec = trampoline

  def execute(json: Json) = {
    val msg = json.noSpaces
    val m = Message(tg.incrementAndGet(), msg)
    config.connectionFactory.get().flatMap(_.execute(m).map(_.body))
  }

  def query(json: Json) = {
    execute(Helper.startQuery(json))
  }

  def run(action: Action) = query(action.term).flatMap(decodeAsFuture[Json]).map(_.head)

  def run[T: Decoder](ast: Sequence) = query(ast.term).flatMap(decodeAsFuture[T])

  def run[T: Decoder](ast: Atom) = query(ast.term).flatMap(decodeAsFuture[T]).map(_.head)

  private def decodeAsFuture[T: Decoder](body: String) = {
    val decodeR = for {
      j <- parse(body): Either[Throwable, Json]
      decoded = j.decodeDates(config.dateTimeFormat)
      rs <- implicitly[Decoder[QueryResult]].decodeJson(decoded): Either[Throwable, QueryResult]
      r <- decodeResult(rs)
    } yield r
    decodeR.fold(e => Future.failed(new Exception(s"Response: $body" ,e)), Future.successful(_))
  }

  private def decodeResult[T: Decoder](r: QueryResult) = {
    (r.r, r.t) match {
      case (Vector(json), ResponseType.SUCCESS_ATOM) if json.isArray =>
        json.asArray.get.traverse(implicitly[Decoder[T]].decodeJson(_))
      case (_, ResponseType.SUCCESS_ATOM|ResponseType.SUCCESS_SEQUENCE | ResponseType.SUCCESS_PARTIAL) =>
        r.r.traverse(implicitly[Decoder[T]].decodeJson(_))
      case _ =>
        Left(new Exception(s"Uknown response type ${r.t}"))
    }
  }
}
