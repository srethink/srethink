package org.srethink.ast

import io.circe._
import org.srethink.net._

object Helper {

  def term(tpe: Int, args: Seq[Json], options: Seq[(String, Json)]) = {
    if(options.isEmpty)
      Json.array(Json.int(tpe), Json.array(args: _*))
    else
      Json.array(Json.int(tpe), Json.array(args: _*), Json.obj(options: _*))
  }

  def makeArray(elements: Seq[Json]) = {
    term(TermConstant.MAKE_ARRAY_VALUE, elements, Nil)
  }
}
