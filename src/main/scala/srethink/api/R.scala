package srethink.api

object r {
  def table(name: String, database: Option[String] = None) = new DSL(AstHelper.table(name, database))
}
