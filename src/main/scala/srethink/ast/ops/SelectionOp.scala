package srethink.ast.ops

import srethink.net._

trait SelectionOp extends RethinkOp {
  trait SelectionDef {
    val parent: JsValue
    def as[A: JsDecoder](implicit executor: QueryExecutor) = decodeR[A](atom(parent))
    def run(implicit executor: QueryExecutor) = atom(parent)
  }
}
