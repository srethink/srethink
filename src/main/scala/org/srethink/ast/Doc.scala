package org.srethink.ast

import io.circe._
import io.circe.syntax._
import org.srethink.net._


trait DocLike extends Ast {

  def selectDynamic(field: String) = {
    val t = Helper.term(
      TermType.GET_FIELD,
      Seq(term, Json.fromString(field))
    )
    Doc(t)
  }

  def ===[V: Encoder](v: V) = {
    val t = Helper.term(TermType.EQ, Seq(term, v.asJson))
    Datum.as[Boolean](t)
  }

  def ===(ast: Ast) = {
    val t = Helper.term(TermType.EQ, Seq(term, ast.term))
    Datum.as[Boolean](t)
  }

  def =!=[V: Encoder](v: V) = {
    val t = Helper.term(TermType.NE, Seq(term, v.asJson))
    Datum.as[Boolean](t)
  }

  def unary_! = {
    val t = Helper.term(TermType.NOT, Seq(this.term))
    Datum.as[Boolean](t)
  }

  def &&(that: Datum[Boolean]) = {
    val t = Helper.term(TermType.AND, Seq(this.term, that.term))
    Datum.as[Boolean](t)
  }

  def ||(that: Datum[Boolean]) = {
    val t = Helper.term(TermType.OR, Seq(this.term, that.term))
    Datum.as[Boolean](t)
  }

  def > [V: Encoder](that: V) = {
    val t = Helper.term(TermType.GT, Seq(this.term, that.asJson))
    Datum.as[Boolean](t)
  }

  def >=[V: Encoder](that: V) = {
    val t = Helper.term(TermType.GE, Seq(this.term, that.asJson))
    Datum.as[Boolean](t)
  }

  def <[V: Encoder](that: V) = {
    val t = Helper.term(TermType.LT, Seq(this.term, that.asJson))
    Datum.as[Boolean](t)
  }

  def <=[V:Encoder](that: V) = {
    val t = Helper.term(TermType.LE, Seq(this.term, that.asJson))
    Datum.as[Boolean](t)
  }

  def as[T] = Datum.as[T](this.term)
}

case class Doc(term: Json) extends DocLike with Dynamic
