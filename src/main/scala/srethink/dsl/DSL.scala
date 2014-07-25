package srethink.dsl

import srethink.ast._
import srethink.api._

trait DSL extends Any {
  private[srethink] def term: RTerm
}

 class Atom (val term: RTerm) extends DSL
