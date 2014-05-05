package srethink.core.codec

import org.specs2.mutable._
import srethink.core._




case class MacorSpecFoo(
  foo: Int
)

class CodecMacroSpec extends Specification {

  implicit object IntEncoder extends DatumEncoder[Int] {
    def encode(i: Int) = ???
  }

  val en = CodecsMacro.encoder[MacorSpecFoo]
}
