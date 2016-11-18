package org.srethink.ast

import io.circe._
import io.circe.syntax._
import org.srethink.net._

trait Global {

  case class DBCreate(name: String) extends Action {
    def term = Helper.term(
      TermType.DB_CREATE,
      Seq(Json.fromString(name))
    )
  }

  case class DBDrop(name: String) extends Action {
    def term = Helper.term(
      TermType.DB_DROP,
      Seq(Json.fromString(name))
    )
  }



  def epoch(time: Long) = new Ast {
    def term = Helper.term(TermType.EPOCH_TIME, Seq(Json.fromBigDecimal(BigDecimal(time) / 1000)))
  }

  def now() = epoch(System.currentTimeMillis)

  def opt[E: Encoder](name: String, value: E) = Opt(name, value.asJson)

  def asc(field: String, opts: Opt*) = Order(field, true, opts: _*)

  def desc(field: String, opts: Opt*) = Order(field, false, opts: _*)

}
