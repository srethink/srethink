package srethink.core.codec

import scala.reflect.macros.Context
import scala.language.experimental.macros
import srethink.protocol._
import srethink.core._

object CodecsMacro {

  def encoder[T] = macro encoderImpl[T]

  def encoderImpl[T: c.WeakTypeTag](c: Context): c.Expr[DatumEncoder[T]]  = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val pairs =  weakTypeOf[T].declarations.collect {
      case m: MethodSymbol if m.isCaseAccessor =>
        val name = c.literal(m.name.decoded)
        val value = q"t.$m.name"
        val valueType = m.returnType
        val encoderType = appliedType(weakTypeOf[DatumEncoder[Any]].typeConstructor, valueType :: Nil )
        val neededImplicit = c.inferImplicitValue(encoderType)
        val encodedValue = q"neededImplicit.encode($value)"
        q"Datum.AssocPair(Some($name), Some($encodedValue))"
    }
    val encodedValue = q"Datum(Some(Datum.DatumType.R_OBJECT), Some(pairs))"
    val method = q"def encode(t: $tpe) = $encodedValue"

    c.Expr[DatumEncoder[T]](q"""
     new DatumEncoder[$tpe] {
       def encode(t: $tpe) = $method
     }""")
  }

  def encodeImpl[T: c.WeakTypeTag](c: Context)(t: c.Expr[T]) = {
    import c.universe._

  }
}
