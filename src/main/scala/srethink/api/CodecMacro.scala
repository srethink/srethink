package srethink.api

import scala.reflect.macros.Context
import scala.language.experimental.macros

object CodecMacros {

  def encoder[T] = macro encoderImpl[T]

  def encoderImpl[T: c.WeakTypeTag](c: Context): c.Expr[REncoder[T]]  = {
    import c.universe._
    val tpe = weakTypeOf[T]
    def inferEncoder(tpe1: Type) = {
      tpe1 match {
        case TypeRef(_, t, arg :: Nil)
            if tpe1.typeConstructor =:= typeOf[Option[_]].typeConstructor && arg =:= tpe =>
          val thisEncoder = This(newTypeName(""))
          q"srethink.api.optionEncoder($thisEncoder)"
        case TypeRef(_, t, arg :: Nil)
            if tpe1.typeConstructor <:< typeOf[Traversable[_]].typeConstructor && arg =:= tpe  =>
          val thisEncoder = This(newTypeName(""))
          q"srethink.api.traversableEncoder($thisEncoder)"
        case TypeRef(_, _, _) =>
          val encoderType = appliedType(typeOf[REncoder[_]], tpe1 :: Nil)
          c.inferImplicitValue(encoderType)
        case _ => EmptyTree
      }
    }
    val tree =  tpe.declarations.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }
    val datum = q"srethink.ast.RDatum"
    val pairs = tree.foldLeft(q"scala.collection.immutable.Seq[(String, RDatum)]()") {
      case (pairsTree, accessorTree) =>
        val valueType = accessorTree.returnType
        val encoder = inferEncoder(valueType)
        if(encoder == EmptyTree) {
          c.abort(c.enclosingPosition, s"No implicit encoder for ${valueType}")
        }
        val name = accessorTree.name.decoded
        val value = q"t.$accessorTree"
        q"$pairsTree :+ $name -> $encoder.encode($value)"
    }
    val encodedDatum = q"new srethink.ast.RObject($pairs)"
    val method = q"def encode(t: $tpe) = $encodedDatum"
    val encoderTree = q"""
     import srethink.ast._
     new REncoder[$tpe] {
       $method
     }"""
    c.Expr[REncoder[T]](encoderTree)
  }
}
