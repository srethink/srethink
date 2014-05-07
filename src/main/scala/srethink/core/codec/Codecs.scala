package srethink.core.codec

import scala.language.experimental.macros
import srethink.core._

object Codecs {
  def encoder[T]: DatumEncoder[T] = macro CodecMacro.encoderImpl[T]
}
