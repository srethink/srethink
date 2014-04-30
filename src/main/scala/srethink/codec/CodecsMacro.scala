import scala.reflect.macros.Context
import scala.language.experimental.macros
import srethink.protocol._
import srethink.codec._

object CodecsMacro {

  def encoderImpl[T: c.WeakTypeTag](c: Context) = {

  }

  def encodeImpl[T: c.WeakTypeTag](t: T)(c: Context) = {
    import c.universe._
    val pairs =  weakTypeOf[T].declarations.collect {
      case m: MethodSymbol if m.isCaseAccessor =>
        val name = c.literal(m.name.decoded)
        val value = q"t.$m.name"
        q"Datum.AssocPair(Some($name), Some($value))"
    }
  }
}
