package srethink.ast.ops

import srethink.json._
import srethink.ast._
import srethink.net._

trait InsertOp  extends RethinkOp {

  trait InsertDef {
    val parent: JsValue
    def insert[A: JsEncoder]
      (docs: Seq[A], opts: o.RInsertOption*)
      (implicit executor: QueryExecutor) = {

      val data = docs.map{a => encode(a)}
      val term = rInsert(parent, data, o.options(opts))
      decodeR[InsertResult](atom(term))
    }

    def insert
      (docs: JsArray, opts: o.RInsertOption*)
      (implicit executor: QueryExecutor) = {
      val data = unapplyJsArray(docs)
      val term = rInsert(parent, data, o.options(opts))
      decodeR[InsertResult](atom(term))
    }
  }
}
