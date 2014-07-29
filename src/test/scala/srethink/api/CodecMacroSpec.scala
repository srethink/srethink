package srethink.api

import org.specs2.mutable.Specification
import srethink.protocol._

case class CodecFoo(
  name: String,
  bar: Option[CodecFoo],
  baz: List[CodecFoo],
  seq: Seq[String],
  set: Set[Int]
)

class CodecMacroSpec extends Specification {
  "codec macro" should {
    "encode/decode of case class" in {
      val encoder = CodecMacros.encoder[CodecFoo]
      val decoder = CodecMacros.decoder[CodecFoo]
      val bar = CodecFoo("bar", None, Nil, Nil, Set.empty[Int])
      val foo = CodecFoo("foo", Some(bar), baz = bar:: Nil, seq = "foo" +: Nil, Set(1, 2, 3))
      val encoded = encoder.encode(foo)
      val decoded = decoder.decode(Some(encoded.toDatum))
      decoded must beSome(foo)
    }
  }
}
