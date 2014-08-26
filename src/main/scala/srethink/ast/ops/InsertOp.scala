package srethink.ast.ops

import srethink.json._
import srethink.ast._
import srethink.net._

trait InsertOp  extends RethinkOp {

  trait Insert {
    val self: JsValue
    def insert[A: JsEncoder]
      (docs: Seq[A], opts: o.RInsertOption*)
      (implicit executor: QueryExecutor) = {

      val data = docs.map{a => encode(a)}
      val term = rInsert(self, data, o.options(opts))
      atom[InsertResult](term)
    }
  }
}
