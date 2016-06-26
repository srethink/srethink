package org.srethink.exec

import cats.data.Xor
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import java.util.concurrent.atomic.AtomicLong
import org.srethink.ast._
import org.srethink.net._
import org.srethink.util.circe._

import scala.concurrent.Future

case class ExecConfig(
  connectionFactory: ConnectionFactory,
  dateTimeFormat: String = "yyyy-MM-dd HH:mm:ss",
  timezone: String)

class QueryExec(val config: ExecConfig) {
  val tg = new AtomicLong(0)
  implicit val ec = trampoline

  def execute(json: Json) = {
    val m = Message(tg.incrementAndGet(), json.noSpaces)
    config.connectionFactory.get().execute(m).map(_.body)
  }

  def run[T: Decoder](ast: Sequence) = execute(ast.term).flatMap(decodeAsFuture[Seq[T]])

  def run[T: Decoder](ast: Atom) = execute(ast.term).flatMap(decodeAsFuture[T])

  def run(action: Action) = execute(action.term).flatMap(decodeAsFuture[Json])

  private def decodeAsFuture[T: Decoder](body: String) = {
    val decodeR = for {
      j <- parse(body)
      decoded = j.decodeDates(config.dateTimeFormat)
      r <- implicitly[Decoder[T]].decodeJson(decoded)
    } yield r
    decodeR.fold(Future.failed(_), Future.successful(_))
  }
}
