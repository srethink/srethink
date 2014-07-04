package srethink.ast

import srethink.protocol._

object AstHelper {

  @inline def strTerm(str: String) = {
    new DatumTerm(new RStr(str))
  }

  @inline def numTerm(num: Double) = {
    new DatumTerm(new RNum(num))
  }

  @inline def function(argc: Int)(func: Seq[Var] => RTerm) = {
    val argIdx = (0 until argc)
    val argIds = new DatumTerm(new RArray(argIdx.map(i => new RNum(i))))
    val vars = argIdx.map(i => new Var(i))
    Func(argIds, func(vars))
  }

  @inline def function1(body: Var => RTerm) = {
    function(1) { args =>
      body(args(0))
    }
  }
}
