package srethink.api

import scala.reflect.macros.Context
import scala.language.experimental.macros

object CodecMacros {

  def encoderImpl[T: c.WeakTypeTag](c: Context): c.Expr[REncoder[T]]  = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val tree =  tpe.declarations.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }
    val datum = q"srethink.ast.RDatum"
    val pairs = tree.foldLeft(q"scala.collection.immutable.Seq[(String, RDatum)]()") {
      case (pairsTree, accessorTree) =>
        val valueType = accessorTree.returnType
        val encoderType = appliedType(typeOf[REncoder[_]], valueType:: Nil)
        val encoder = c.inferImplicitValue(encoderType)
        if(encoder == EmptyTree) {
          c.abort(c.enclosingPosition, s"No implicit encoder for ${encoderType}")
        }
        val name = accessorTree.name.decoded
        val value = q"t.$accessorTree"
        q"$pairsTree :+ $name -> $encoder.encode($value).getOrElse(new srethink.api.CodecException($name))"
    }
    val encodedDatum = q"$datum(`type` = Some($datum.DatumType.R_OBJECT), rObject = $pairs )"
    val method = q"def encode(t: $tpe) = $encodedDatum"
    val encoderTree = q"""
     import srethink.protocol._
     import srethink.core._
     new DatumEncoder[$tpe] {
       $method
     }"""
    c.Expr[REncoder[T]](encoderTree)
  }
}
