package srethink.api

import srethink.dsl._
import srethink.ast.AstHelper
object r {
  def table(name: String, database: Option[String] = None) = new TableDSL(AstHelper.table(name, database))
}
