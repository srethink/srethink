package srethink.ast

import srethink.ast.ops._
import srethink.json._

trait OpsInstances extends Ast
    with InsertOp
    with TableCreateOp
    with TableDropOp
    with DBCreateOp
    with DBDropOp {

  implicit class TableOpsInstance(val table: RTable) extends Insert  {
    val tableTerm = rTable(table.db, table.name)
    val self = tableTerm
  }

  implicit class RootOpsInstance(root: r.type)
      extends TableCreate
      with TableDrop
      with DBCreate
      with DBDrop {
    def table(db: String, name: String, options: o.RTableOption*) = new RTable(db, name, options)
  }
}
