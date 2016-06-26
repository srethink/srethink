package org.srethink.ast

import io.circe._

trait Datum[V] extends Any

class BooleanDatum(val value: Boolean) extends AnyVal with Datum[Boolean]
class IntDatum(val value: Int) extends  AnyVal with Datum[Int]
class LongDatum(val value: Long) extends AnyVal with Datum[Long]
class StringDatum(val value: String) extends AnyVal with Datum[String]


object Datum {
  def boolean(t: Json) = new Datum[Boolean] {
    def term = t
  }
}
