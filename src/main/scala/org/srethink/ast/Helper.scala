package org.srethink.ast

import io.circe._
import org.srethink.net._

object Helper {

  def term(tpe: Int, args: Seq[Json], options: Seq[(String, Json)]) = {
    if(options.isEmpty)
      Json.arr(Json.fromInt(tpe), Json.arr(args: _*))
    else
      Json.arr(Json.fromInt(tpe), Json.arr(args: _*), Json.obj(options: _*))
  }

  def makeArray(elements: Seq[Json]) = {
    term(TermType.MAKE_ARRAY, elements, Nil)
  }
}
