package srethink.json

import java.util.Date
import scala.collection.generic._
/**
 * This trait aims to support multi json serialize library
 */
trait JsonDef[J, F[_]] {

  implicit val booleanF: F[Boolean]
  implicit val intF: F[Int]
  implicit val longF: F[Long]
  implicit val floatF: F[Float]
  implicit val doubleF: F[Double]
  implicit val stringF: F[String]
  implicit val dateF: F[Date]
  implicit def jsNumber(i: Long): J = encode(i)
  implicit def jsNumber(d: Double): J = encode(d)
  implicit def jsBool(b: Boolean): J = encode(b)
  implicit def jsString(s: String): J = encode(s)

  def encode[T: F](t: T): J
  def decode[T: F](json: J): T

  def parse(s: String): J
  def stringify(j: J): String

  implicit def jsArray(seq: Seq[J]): J
  implicit def jsObject(seq: Seq[(String, J)]): J
  def unapplyJsArray(j: J): Seq[J]
  def unapplyJsObject(j: J): Seq[(String, J)]
}
