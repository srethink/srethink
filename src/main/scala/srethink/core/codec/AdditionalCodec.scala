package srethink.core.codec

import srethink.protocol._
import srethink.core._
import CodecHelper._

trait AdditionalEncoders {
  implicit def  traversableEncoder[T: DatumEncoder] = {
    new DatumEncoder[Traversable[T]] {
      def encode(t: Traversable[T]) = {
        arrDatum(t.map(Codecs.encode[T]).to[collection.immutable.Seq])
      }
    }
  }

  implicit def optionEncoder[T: DatumEncoder] = {
    new DatumEncoder[Option[T]] {
      def encode(t: Option[T]) = {
        t.map(Codecs.encode[T]).getOrElse(nullDatum())
      }
    }
  }
}
