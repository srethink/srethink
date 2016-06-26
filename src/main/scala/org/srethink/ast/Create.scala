package org.srethink.ast

import io.circe._
import org.srethink.net._

case class DBCreate(name: String) extends Action {
  def term = Helper.term(
    TermType.DB_CREATE,
    Seq(Json.fromString(name))
  )
}

case class TableCreate(db: DB, name: String, opts: Seq[Opt]) extends Action {
  def term = Helper.term(
    TermType.TABLE_CREATE,
    Seq(db.term, Json.fromString(name)),
    opts.map(_.pair))
}

case class DBDrop(name: String) extends Action {
  def term = Helper.term(
    TermType.DB_DROP,
    Seq(Json.fromString(name))
  )
}

case class TableDrop(db: DB, name: String) extends Action {
  def term = Helper.term(
    TermType.TABLE_DROP,
    Seq(db.term, Json.fromString(name))
  )
}
