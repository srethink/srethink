package srethink.ast.ops

import srethink.ast._
import srethink.net._

trait IndexDropOp extends RethinkOp with Ast {

  trait IndexDropDef {
    val parent: JsValue

    def indexDrop(name: String)(implicit queyExecutor: QueryExecutor) = {
      val term = rIndexDrop(parent, name)
      decodeR[DropResult](atom(term))
    }
  }

}
