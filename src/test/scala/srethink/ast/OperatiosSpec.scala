package srethink.ast

import srethink._

class OperatorsSpec extends RethinkOperatorSpec {
  "rql" should {
    testOp("add")(_.quantity + 1)(_.quantity + 1)
    testOp("sub")(_.quantity - 1)(_.quantity - 1)
    testOp("mul")(_.quantity * 10)(_.quantity * 10)
    testOp("div")(_.quantity / 2.0)(_.quantity / 2.0)
  }
}
