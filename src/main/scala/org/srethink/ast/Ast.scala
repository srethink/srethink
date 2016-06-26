package org.srethink.ast

import io.circe.Json

trait Ast[A] {
  def show[A: AstShow] = implicitly[AstShow[A]].show(this)
}

case class Db(name: String) {
  def table(name: String, options: (String, Json) *) = {
    Table(this, name, options)
  }
}
case class Table(db: Db, name: String, options: Seq[(String, Json)])
case class Get(table: TABLE, key: Json)
case class GetAll(table: Table, keys: Seq[Json])
