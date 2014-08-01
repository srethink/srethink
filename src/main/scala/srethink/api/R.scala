package srethink.api

object r {
  def table(name: String, database: String) = new DSL(AstHelper.table(name, database))
}
