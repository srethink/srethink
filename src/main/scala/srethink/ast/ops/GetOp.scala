package srethink.ast.ops

import srethink.net._
import srethink.ast._

trait GetOp extends RethinkOp with Ast {
  trait GetDef {
    val parent: JsValue
    def get[A: JsEncoder] (key: A) = {
      val jsKey = encode(key)
      Get(rGet(parent, jsKey))
    }
  }
}
