package srethink.core.api

import srethink.core.ast._

trait TableOps {
  val database: RDatabase
  val table: RTable

  private def builder() = {
    Select.builder(database, table)
  }

  def get[T: RConstant](primaryKey: T) = {
      builder().filter(table.primaryKey === primaryKey)
  }

  def filter(func: RTable => Cond) = {
    builder().filter(func(table))
  }

  def map(func: RTable => Expr) = {
    builder.map(func(table))
  }
}
