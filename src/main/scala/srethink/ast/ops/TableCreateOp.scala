package srethink.ast.ops

import srethink.ast._
import srethink.net._

trait TableCreateOp extends RethinkOp {

  trait TableCreate {
    def tableCreate(db: String, table: String, opts: o.RTableCreateOption*)(implicit executor: QueryExecutor) = {
      val term = rTableCreate(rDatabase(db), table,  o.options(opts))
      atom[CreateResult](term)
    }
  }
}
