package org.srethink.ast

import io.circe._
import io.circe.syntax._
import org.srethink.net._
import org.srethink.util.circe._

case class Table(db: DB, name: String, options: Seq[Opt]) extends Sequence {

  def term = Helper.term(
    TermType.TABLE,
    Seq(db.term, Json.fromString(name)),
    options.map(_.pair))

  def get[K: Encoder](key: K) = Get(this, key.asJson.encodeArray)

  def getAll[K: Encoder](keys: Seq[K], options: Opt*) = GetAll(this, keys.map(_.asJson.encodeArray()), options)

  def insert[V: Encoder](vs: V*) = Insert(this, vs.map(_.asJson))

}

case class Insert(table: Table, values: Seq[Json]) extends Action {
  def term = Helper.term(TermType.INSERT, Seq(table.term, Helper.makeArray(values)))
}

case class Get(table: Table, key: Json) extends Atom {
  def term = Helper.term(TermType.GET, Seq(table.term, key))
}
case class GetAll(table: Table, keys: Seq[Json], opts: Seq[Opt]) extends Sequence {
  def term = Helper.term(TermType.GET_ALL, table.term +: keys, opts.map(_.pair))
}