package srethink.api

import org.specs2.mutable.Specification
import srethink.ast._

case class CodecFoo(
  name: String,
  bar: Option[CodecFoo],
  baz: List[CodecFoo]
)

class CodecMacroSpec extends Specification {
  "codec macro" should {
    "generate encoder of case class" in {
      val encoder = CodecMacros.encoder[CodecFoo]
      val data: RDatum = new RStr("foo")
      val bar = CodecFoo("bar", None, Nil)
      val encoded = encoder.encode(CodecFoo("foo", Some(bar), baz = bar:: Nil))
      println(encoded.value)
      encoded.value must have size(3)
    }
  }
}
