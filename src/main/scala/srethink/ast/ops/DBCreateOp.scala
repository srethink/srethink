package srethink.ast.ops

import srethink.ast._
import srethink.net._

trait DBCreateOp extends RethinkOp {

  trait DBCreateDef {
    def dbCreate(name: String)(implicit executor: QueryExecutor) = {
      val term = rDBCreate(name)
      decodeR[CreateResult](atom(term))
    }
  }
}
