package srethink.ast.ops

import srethink.ast._
import srethink.net._

trait DBDropOp extends RethinkOp {

  trait DBDropDef {
    def dbDrop(db: String)(implicit executor: QueryExecutor) = {
      val term = rDBDrop(db)
      decodeR[CreateResult](atom(term))
    }
  }
}
