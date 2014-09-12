package srethink.ast

import srethink.json._
import ops._

trait Ast extends ROptions with Terms with GetFieldOp {
  object r
  case class RTable(db: String, name: String, options: Seq[o.RTableOption])
  case class Get(term: JsValue)
  case class GetAll(term: JsValue, options: Seq[o.RGetAllOption]*)
  case class Expr(term: JsValue) extends Dynamic with GetFieldDef{
    val parent = term
  }
  case class RFunction(term: JsValue)
}
