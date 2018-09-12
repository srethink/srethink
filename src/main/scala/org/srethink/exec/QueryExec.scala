package org.srethink.exec

import cats.effect._
import cats.syntax.all._
import io.circe._
import cats.instances.vector._
import cats.instances.either._
import io.circe.parser._
import io.circe.generic.auto._
import java.util.concurrent.atomic.AtomicLong
import org.srethink.ast._
import org.srethink.net._
import org.srethink.util.circe._

case class QueryResult(t: Int, r: Vector[Json], b: Option[String])

case class ExecConfig[F[_]](
  connectionFactory: ConnectionFactory[F],
  dateTimeFormat: String = "yyyy-MM-dd HH:mm:ss",
  timezone: String)

class QueryExec[F[_]: ConcurrentEffect](val config: ExecConfig[F]) {
  val tg = new AtomicLong(0)
  def execute(json: Json) = {
    val msg = json.pretty(Printer.noSpaces.copy(dropNullValues = true))
    val m = Message(tg.incrementAndGet(), msg)
    config.connectionFactory.get().flatMap(_.execute(m).map(_.body))
  }

  def query(json: Json) = {
    execute(Helper.startQuery(json))
  }

  def run(action: Action) = query(action.term).map(decode[Json]).map {
    _.map(_.head)
  }

  def run[T: Decoder](ast: Sequence) = query(ast.term).map(decode[T]).rethrow

  def run[T: Decoder](ast: Atom) = query(ast.term).map(decode[T]).rethrow.map(_.head)

  private def decode[T: Decoder](body: String) = {
    for {
      j <- parse(body): Either[Throwable, Json]
      decoded = j.decodeDates(config.dateTimeFormat)
      rs <- implicitly[Decoder[QueryResult]].decodeJson(decoded): Either[Throwable, QueryResult]
      r <- decodeResult(rs)
    } yield r
  }

  private def decodeResult[T: Decoder](r: QueryResult) = {
    (r.r, r.t) match {
      case (Vector(json), ResponseType.SUCCESS_ATOM) if json.isArray =>
        json.asArray.get.traverse(_.as[T])
      case (_, ResponseType.SUCCESS_ATOM|ResponseType.SUCCESS_SEQUENCE | ResponseType.SUCCESS_PARTIAL) =>
        r.r.traverse(_.as[T])
      case _ =>
        Left(new Exception(s"Uknown response type ${r.t}"))
    }
  }
}
