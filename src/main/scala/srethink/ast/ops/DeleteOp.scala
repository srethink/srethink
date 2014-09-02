package srethink.ast.ops

import srethink.ast._
import srethink.net._

trait DeleteOp extends RethinkOp {
  trait DeleteDef {
    val parent: JsValue

    def delete(opts: o.RDeleteOption*)(implicit executor: QueryExecutor) = {
      decodeR[DeleteResult](atom(rDelete(parent, o.options(opts))))
    }
  }
}
