package srethink.core.codec

import org.specs2.mutable._
import srethink.core._
import CodecHelper._

case class MacroSpecFoo(
  foo: Int
)

case class MacroSpecBar(
  foo: Int,
  bar: MacroSpecBar
)

class CodecMacroSpec extends Specification {

  implicit object IntEncoder extends DatumEncoder[Int] {
    def encode(i: Int) = numDatum(i)
  }

  "encoder macro" should {
    "create generate encoders" in {
      val en = CodecMacro.encoder[MacroSpecFoo]
      val pairs = en.encode(MacroSpecFoo(1)).rObject
      pairs must have size(1)
      pairs(0).`val` must beSome(numDatum(1))
    }
  }
}
