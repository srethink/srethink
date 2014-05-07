package srethink.core.codec

import org.specs2.mutable._
import org.specs2.specification._
import srethink.core._
import scala.collection.immutable._
import CodecHelper._
import Codecs._


case class MacroSpecFoo(
  foo: Int
)

case class MacroSpecBar(
  foo: Int,
  bar: MacroSpecFoo
)

class CodecMacroSpec extends Specification {
  val foo = MacroSpecFoo(1)
  val bar = MacroSpecBar(2, foo)
  val fooDatum = objDatum(Seq(assocPair("foo", numDatum(1))))
  val barDatum = objDatum(Seq(assocPair("foo", numDatum(2)), assocPair("bar", fooDatum)))

  implicit val fooEncoder = encoder[MacroSpecFoo]
  implicit val barEncoder = encoder[MacroSpecBar]
  implicit val fooDecoder = decoder[MacroSpecFoo]
  implicit val barDecoder = decoder[MacroSpecBar]

  "encoder macro" should {
    "encode and decode simple class" in {
      decode[MacroSpecFoo](encode(foo)) mustEqual(foo)
    }

    "encode/decode nest class" in {
      decode[MacroSpecBar](encode(bar)) mustEqual(bar)
    }

    "generate list encoder" in {
      val datum =  Codecs.encode(Seq(foo)).rArray
      datum must contain(exactly(fooDatum))
    }

    "generate option encoder" in {
      val datum = Codecs.encode(Some(foo))
      datum mustEqual(fooDatum)
    }
  }
}
