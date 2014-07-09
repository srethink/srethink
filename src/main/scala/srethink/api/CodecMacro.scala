package srethink.api

import scala.reflect.macros.Context
import scala.language.experimental.macros

object CodecMacros {

  def encoder[T] = macro encoderImpl[T]

  def encoderImpl[T: c.WeakTypeTag](c: Context): c.Expr[REncoder[T]]  = {
    val helper = new EncoderMacroHelper[c.type](c)
    helper.encoderImpl[T]
  }

  def decoder[T] = macro decoderImpl[T]

  def decoderImpl[T: c.WeakTypeTag](c: Context): c.Expr[RDecoder[T]] = {
    val helper = new DecoderMacroHelper[c.type](c)
    helper.decoderImpl[T]
  }
}

class EncoderMacroHelper[C <: Context](val c: C) {
  import c.universe._
  def encoderImpl[T: c.WeakTypeTag]: c.Expr[REncoder[T]]  = {
    val tpe = weakTypeOf[T]
    val method = encodeMethod(tpe)
    val encoderTree = q"""
     import srethink.ast._
     new REncoder[$tpe] {
       $method
     }"""
    c.Expr[REncoder[T]](encoderTree)
  }

  def encodeMethod(tpe: Type) = {
    val tree =  tpe.declarations.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }
    val datum = q"srethink.ast.RDatum"
    val pairs = tree.foldLeft(q"scala.collection.immutable.Seq[(String, RDatum)]()") {
      case (pairsTree, accessorTree) =>
        val valueType = accessorTree.returnType
        val encoder = inferEncoder(valueType, tpe)
        if(encoder == EmptyTree) {
          c.abort(c.enclosingPosition, s"No implicit encoder for ${valueType}")
        }
        val name = accessorTree.name.decoded
        val value = q"t.$accessorTree"
        q"$pairsTree :+ $name -> $encoder.encode($value)"
    }
    val encodedDatum = q"new srethink.ast.RObject($pairs)"
    q"def encode(t: $tpe) = $encodedDatum"

  }

  def inferEncoder(tpe1: Type, objType: Type) = {
    tpe1 match {
      case TypeRef(_, t, arg :: Nil)
          if tpe1.typeConstructor =:= typeOf[Option[_]].typeConstructor && arg =:= objType =>
        val thisEncoder = This(newTypeName(""))
        q"srethink.api.optionEncoder($thisEncoder)"
      case TypeRef(_, t, arg :: Nil)
          if tpe1.typeConstructor <:< typeOf[Traversable[_]].typeConstructor && arg =:= objType  =>
        val thisEncoder = This(newTypeName(""))
        q"srethink.api.traversableEncoder($thisEncoder)"
      case TypeRef(_, _, _) =>
        val encoderType = appliedType(typeOf[REncoder[_]], tpe1 :: Nil)
        c.inferImplicitValue(encoderType)
      case _ => EmptyTree
    }
  }
}

class DecoderMacroHelper[C <: Context](val c: C) {
  import c.universe._
  def decoderImpl[T: c.WeakTypeTag]: c.Expr[RDecoder[T]] = {
    val tpe = c.weakTypeOf[T]
    val method = decodeMethod(tpe)
    val decoderTree = q"""
      import srethink.protocol._
      import srethink.api._
      new RDecoder[$tpe] {
        $method
      }"""
    c.Expr[RDecoder[T]](decoderTree)
  }

  def decodeMethod(tpe: Type) = {
    val companion = tpe.typeSymbol.companionSymbol
    val datum = tq"srethink.protocol.Datum"
    val ctor = tpe.declarations.collectFirst { case m: MethodSymbol if m.isPrimaryConstructor => m }.get
    val params = ctor.paramss.head
    val paramValues = params.map { param =>
      val name = param.name.decoded
      val valueType = param.typeSignature
      val valueTypeName = valueType.toString
      val decoder = inferDecoder(valueType, tpe)
      if(decoder == EmptyTree) {
        c.abort(c.enclosingPosition, s"No implicit encoder for ${name}")
      }
      val newCodecException = q"throw new CodecException($name, $valueTypeName, pairMap.get($name) )"
      q"$decoder.decode(pairMap.get($name)).getOrElse($newCodecException)"
    }
    val decodedValue = q"Some($companion(..$paramValues))"
    q"""
       def decode(t: Option[$datum]) = {
         t.flatMap { d =>
           d.`type` match {
             case Some(Datum.DatumType.R_OBJECT) =>
             val pairMap = d.rObject.collect { case Datum.AssocPair(Some(key), Some(value)) => key -> value}.toMap
             $decodedValue
             case _ => None
           }
         }
       }
    """

  }

  def inferDecoder(valueType: Type, objType: Type) = {
    valueType match {
      case TypeRef(_, t, arg :: Nil)
          if valueType.typeConstructor =:= typeOf[Option[_]].typeConstructor && arg =:= objType =>
        val thisDecoder = This(newTypeName(""))
        q"srethink.api.optionDecoder[$objType]($thisDecoder)"
      case TypeRef(_, t, arg :: Nil)
          if valueType.typeConstructor <:< typeOf[Traversable[_]].typeConstructor && arg =:= objType  =>
        val thisDecoder = This(newTypeName(""))
        val collType = valueType.typeConstructor.typeSymbol
        val cbf = q"implicitly[collection.generic.CanBuildFrom[$collType[_], $arg, $valueType]]"
        q"srethink.api.traversableDecoder[$collType, $arg]($cbf, $thisDecoder)"
      case TypeRef(_, _, _) =>
        val encoderType = appliedType(typeOf[RDecoder[_]], valueType :: Nil)
        c.inferImplicitValue(encoderType)
      case _ => EmptyTree
    }
  }
}
