package srethink.ast.ops

import srethink.ast._
import srethink.net._

trait IndexCreateOp extends RethinkOp with Ast {

  trait IndexCreateDef {
    val parent: JsValue

    def indexCreate(name: String)(func: Expr => Expr)(implicit queyExecutor: QueryExecutor) = {
      val funcTerm = rFunc(1, func(Expr(rVar(1))).term)
      val term = rIndexCreate(parent, name, funcTerm)
      decodeR[CreateResult](atom(term))
    }
  }

}
