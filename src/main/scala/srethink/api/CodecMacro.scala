package srethink.api

import scala.reflect.macros.Context
import scala.language.experimental.macros

object CodecMacros {

  def encoderImpl[T: c.WeakTypeTag](c: Context): c.Expr[ObjectEncoder[T]]  = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val tree =  tpe.declarations.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }
    val datum = q"srethink.protocol.Datum"
    val pairs = tree.foldLeft(q"scala.collection.immutable.Seq[$datum.AssocPair]()") {
      case (pairsTree, accessorTree) =>
        val valueType = accessorTree.returnType
        val encoderType = appliedType(typeOf[ObjectEncoder[_]], valueType:: Nil)
        val encoder = c.inferImplicitValue(encoderType)
        if(encoder == EmptyTree) {
          c.abort(c.enclosingPosition, s"No implicit encoder for ${encoderType}")
        }
        val name = accessorTree.name.decoded
        val value = q"t.$accessorTree"
        q"$pairsTree :+$datum.AssocPair(Some($name), Some($encoder.encode($value)))"
    }
    val encodedDatum = q"$datum(`type` = Some($datum.DatumType.R_OBJECT), rObject = $pairs )"
    val method = q"def encode(t: $tpe) = $encodedDatum"
    val encoderTree = q"""
     import srethink.protocol._
     import srethink.core._
     new DatumEncoder[$tpe] {
       $method
     }"""
    c.Expr[ObjectEncoder[T]](encoderTree)
  }
}
