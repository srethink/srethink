package srethink.core.codec

import scala.reflect.macros.Context
import scala.language.experimental.macros
import srethink.protocol._
import srethink.core._

object CodecMacro {

  def encoderImpl[T: c.WeakTypeTag](c: Context): c.Expr[DatumEncoder[T]]  = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val tree =  tpe.declarations.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }
    val datum = q"srethink.protocol.Datum"
    val pairs = tree.foldLeft(q"scala.collection.immutable.Seq[$datum.AssocPair]()") {
      case (pairsTree, accessorTree) =>
        val valueType = accessorTree.returnType
        val encoderType = appliedType(typeOf[DatumEncoder[_]], valueType:: Nil)
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
    c.Expr[DatumEncoder[T]](encoderTree)
  }

  def decoderImpl[T:c.WeakTypeTag](c: Context): c.Expr[DatumDecoder[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val companion = tpe.typeSymbol.companionSymbol
    val tree =  weakTypeOf[T].declarations.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }
    val datum = tq"srethink.protocol.Datum"

    val ctor = tpe.declarations.collectFirst { case m: MethodSymbol if m.isPrimaryConstructor => m }.get
    val params = ctor.paramss.head
    val paramValues = params.map { param =>
      val name = param.name.decoded
      val valueType = param.typeSignature
      val decoderType = appliedType(typeOf[DatumDecoder[_]], valueType:: Nil)
      val decoder = c.inferImplicitValue(decoderType)
      if(decoder == EmptyTree) {
        c.abort(c.enclosingPosition, s"No implicit encoder for ${decoderType}")
      }
      q"$decoder.decode(pairMap(Some($name)).get)"
    }

    val decodedValue = q"$companion(..$paramValues)"
    val method = q"""
      def decode(d: $datum) ={
        val pairMap = d.`rObject`.map { p => p.`key` -> p.`val` }.toMap
        $decodedValue
      }"""
    val decoderTree = q"""
     import srethink.protocol._
     import srethink.core._
     new DatumDecoder[$tpe] {
       $method
     }"""
    c.Expr[DatumDecoder[T]](decoderTree)
  }
}
