package srethink.core.api

import srethink.core._
import srethink.core.ast._
import srethink.core.codec._

case class Select(
  database: RDatabase,
  table: RTable,
  map: Option[Expr],
  filter: Option[Cond],
  offset: Option[Int],
  size: Option[Int])

object Select {

  def builder(db: RDatabase, table: RTable) = {
    new SelectBuilder(db, table)
  }

  implicit  object SelecEncoder extends QueryEncoder[Select] {
    import CodecHelper._
    def encode(token: Long, api: Select) = {
      ???
    }
  }
}

class SelectBuilder(
  val _db: RDatabase,
  val _table: RTable,
  var _map: Option[Expr] = None,
  var _filter: Option[Cond] = None,
  var _offset: Option[Int] = None,
  var _size: Option[Int] = None
) {

  def map(func: Expr) = {
    _map = Some(func)
    this
  }

  def filter(func: Cond) = {
    _filter = Some(func)
    this
  }

  def drop(offset: Int) = {
    _offset = Some(offset)
    this
  }

  def limit(size: Int) = {
    _size = Some(size)
    this
  }

  def build() = {
     Select(
       database = _db,
       table = _table,
       map = _map,
       filter =  _filter,
       offset = _offset,
       size = _size)
  }
}
