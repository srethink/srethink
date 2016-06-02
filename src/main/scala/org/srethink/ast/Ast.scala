package org.srethink.ast

import io.circe.Json

trait Ast[A] {
  def show[A: AstShow] = implicitly[AstShow[A]].show(this)
}

case class Db(name: String)
case class Table(db: Db, name: String)
case class Get(table: TABLE, key: Json)
case class GetAll(table: Table, keys: Seq[Json])
