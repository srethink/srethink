package srethink.api

import org.specs2.mutable.Specification
import srethink.ast._
import srethink.protocol._

case class CodecFoo(
  name: String,
  bar: Option[CodecFoo],
  baz: List[CodecFoo]
)

class CodecMacroSpec extends Specification {
  "codec macro" should {
    "encode/decode of case class" in {
      val encoder = CodecMacros.encoder[CodecFoo]
      val decoder = CodecMacros.decoder[CodecFoo]
      val bar = CodecFoo("bar", None, Nil)
      val foo = CodecFoo("foo", Some(bar), baz = bar:: Nil)
      val encoded = encoder.encode(foo)
      encoded.toDatum.rObject.foreach {
        case Datum.AssocPair(Some(key), Some(v)) => println(key + "--->" + v)
      }
      val decoded = decoder.decode(Some(encoded.toDatum))
      decoded must be_==(foo)
    }
  }
}
