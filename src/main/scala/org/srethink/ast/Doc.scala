package org.srethink.ast

import io.circe._
import io.circe.syntax._
import org.srethink.net._

case class Doc(term: Json) extends Ast with Dynamic {

  def selectDynamic(field: String) = {
    val t = Helper.term(
      TermType.GET_FIELD,
      Seq(term, Json.fromString(field))
    )
    Doc(t)
  }

  def ===[V: Encoder](v: V) = {
    val t = Helper.term(TermType.EQ, Seq(term, v.asJson))
    Datum.boolean(t)
  }

  def =!=[V: Encoder](v: V) = {
    val t = Helper.term(TermType.NE, Seq(term, v.asJson))
    Datum.boolean(t)
  }
}
