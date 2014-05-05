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
    val tree =  weakTypeOf[T].declarations.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }


    val pairs = tree.foldLeft(q"Seq[Datum.AssocPair]()") {
      case (pairsTree, accessorTree) =>
        val valueType = accessorTree.returnType
        val encoderType = tq"DatumEncoder[$valueType]"
        val encoder = c.inferImplicitValue(encoderType.tpe)
        if(encoder == EmptyTree) {
          c.abort(c.enclosingPosition, s"No implicit encoder for ${encoderType}")
        }
        val value = q"t.$accessorTree"
        q"$pairsTree :+ Datum.AssocPair(Some(accessorTree.name), $encoder.encode($value) )"
    }

    val encodedDatum = q"Datum(`type` = Some(Datum.DatumType.R_OBJECT), rObject = $pairs )"
    val method = q"def encode(t: $tpe) = $encodedDatum"

    c.Expr[DatumEncoder[T]](q"""
     import srethink.protocol._
     new DatumEncoder[$tpe] {
       def encode(t: $tpe) = $method
     }""")
  }

  def encodeImpl[T: c.WeakTypeTag](c: Context)(t: c.Expr[T]) = {
    import c.universe._

  }
}
