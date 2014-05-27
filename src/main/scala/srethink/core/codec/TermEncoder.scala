package srethink.core.codec

import srethink.core._
import srethink.core.ast._
import srethink.protocol._

trait TermEncoder {
  def encode(expr: Expr): Term
}

object DefaultTermEncoders extends TermEncoder{
}
