package srethink.ast

import srethink.json._
import ops._

trait Ast extends ROptions {
  object r
  case class RTable(db: String, name: String, options: Seq[o.RTableOption])
}
