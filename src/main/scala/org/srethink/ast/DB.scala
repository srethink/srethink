package org.srethink.ast

import io.circe._
import org.srethink.net._

case class TableCreate(db: DB, name: String, opts: Seq[Opt]) extends Action {
  def term = Helper.term(
    TermType.TABLE_CREATE,
    Seq(db.term, Json.fromString(name)),
    opts.map(_.pair))
}

case class TableDrop(db: DB, name: String) extends Action {
  def term = Helper.term(
    TermType.TABLE_DROP,
    Seq(db.term, Json.fromString(name))
  )
}

case class DB(name: String)  extends Ast {
  def term = Helper.term(TermType.DB, Seq(Json.fromString(name)))
  def table(name: String, options: Opt*) = {
    Table(this, name, options)
  }

  def tableCreate(name: String, opts: Opt*) = TableCreate(this, name, opts)
  def tableDrop(name: String) = TableDrop(this, name)
}
