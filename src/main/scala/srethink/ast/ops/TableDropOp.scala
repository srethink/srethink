package srethink.ast.ops

import srethink.ast._
import srethink.net._

trait TableDropOp extends RethinkOp {

  trait TableDropDef {
    def tableDrop(table: String, db: String)(implicit executor: QueryExecutor) = {
      val term = rTableDrop(rDatabase(db), table)
      decodeR[DropResult](atom(term))
    }
  }
}
