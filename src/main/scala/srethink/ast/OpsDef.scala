package srethink.ast

import srethink.ast.ops._
import srethink.json._

trait OpsDef extends Ast
    with InsertOp
    with TableCreateOp
    with TableDropOp
    with DBCreateOp
    with DBDropOp
    with DeleteOp
    with GetAllOp
    with SelectionOp
    with GetFieldOp
    with IndexCreateOp with IndexDropOp {

  implicit class TableOpsInstance(val table: RTable)
      extends InsertDef with DeleteDef with GetAllDef with IndexCreateDef with IndexDropDef {
    val tableTerm = rTable(table.db, table.name)
    val parent = tableTerm
  }

  implicit class GetAllInstance(val getAll: GetAll) extends SelectionDef {
    val parent = getAll.term
  }

  implicit class ExprInstance(val expr: Expr) {
    val parent = expr.term
  }

  implicit class RootOpsInstance(root: r.type)
      extends TableCreateDef
      with TableDropDef
      with DBCreateDef
      with DBDropDef {
    def table(db: String, name: String, options: o.RTableOption*) = new RTable(db, name, options)
  }

}
