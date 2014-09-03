package srethink.ast.ops

import srethink.ast._

trait GetFieldOp extends RethinkOp { this: Ast =>
  trait GetFieldDef {
    val parent: JsValue
    def selectDynamic(field: String) = {
      Expr(rGetField(parent, field))
    }
  }
}
