package srethink.dsl
import srethink.ast._

trait DSL {
  private[srethink] def toTerm: RTerm
}
