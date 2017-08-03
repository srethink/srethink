package play.api.rethink

import java.util.Date
import org.slf4j._
import play.api.libs.json._
import play.api.libs.json.Json
import scala.util.control.TailCalls._
import scala.reflect.ClassTag
import srethink.json._
import srethink.ast._
import srethink.protocol._
import srethink._



trait PlayRQL extends AstDef[JsValue, Format] with PlayRethinkFormats {

  private val logger = LoggerFactory.getLogger(classOf[PlayRQL])

  def jsArray(items: Seq[JsValue]) = new JsArray(items)
  def jsObject(fields: Seq[(String, JsValue)]) = new JsObject(fields.toMap)

  def unapplyJsObject(obj: JsValue) = {
    val JsObject(fields) = obj
    fields.toSeq
  }
  def unapplyJsArray(arr: JsValue): Seq[JsValue] = {
    val JsArray(ems) = arr
    ems
  }

  def stringify(js: JsValue) = Json.stringify(js)
  def parse(js: String) = Json.parse(js)
  def encode[T: Format](t: T) = Json.toJson(t)
  def decode[T: Format](json: JsValue) = {
    Json.fromJson[T](json) match {
      case JsSuccess(v, _) => v
      case e@JsError(errs) => throw new Exception(e.errors.mkString("\n"))

    }
  }

  def transformArray(j: JsValue): JsValue = transform(j).result

  private def transform(json: JsValue):TailRec[JsValue] = {
    json match {
      case JsObject(fields) =>
        val transformedFields = fields.map {
          case (k, v) =>
            transform(v).map(k -> _)
        }
        aggregate(transformedFields).map(JsObject(_))
      case v@JsArray(Seq(JsNumber(t), _)) if t.toInt == TermConstant.MAKE_ARRAY_VALUE  => done(v)
      case JsArray(elems) =>
        aggregate(elems.map(e => tailcall(transform(e)))).map { es =>
          JsArray(Seq(JsNumber(TermConstant.MAKE_ARRAY_VALUE),JsArray(es) ))
        }
      case v =>
        done(v)
    }
  }


  private def aggregate[T: ClassTag](recs: Iterable[TailRec[T]]): TailRec[Seq[T]] = {
    val init: Seq[T] = Array.empty[T]
    recs.foldLeft(done(init)) { (rsr, rr ) =>
      for {
        rs <- rsr
        r <- rr
      } yield rs :+ r
    }
  }
}
