package srethink.ast.ops

import srethink.net._
import srethink.ast._

trait GetAllOp extends RethinkOp with Ast {
  trait GetAllDef {
    val parent: JsValue
    def getAll[A: JsEncoder]
      (keys: Seq[A], opts: o.RGetAllOption*) = {
      val jsKeys = keys.map(k => encode(k))
      GetAll(rGetAll(parent, jsKeys, o.options(opts)))
    }
  }
}
