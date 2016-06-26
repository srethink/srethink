package org.srethink.ast

import io.circe._
import org.srethink.net._

object Helper {

  type Opts = Seq[(String, Json)]

  def query(queryType: Int, term: Json, optargs: Opts = Seq.empty) = {
   Json.arr(Json.fromInt(queryType), term, Json.obj(optargs: _*))
  }


  def startQuery(term: Json, optargs: Opts = Seq.empty) = {
    query(QueryType.START, term, optargs)
  }

  def term(tpe: Int, args: Seq[Json], options: Opts = Seq.empty) = {
    if(options.isEmpty)
      Json.arr(Json.fromInt(tpe), Json.arr(args: _*))
    else
      Json.arr(Json.fromInt(tpe), Json.arr(args: _*), Json.obj(options: _*))
  }

  def makeArray(elements: Seq[Json]) = {
    term(TermType.MAKE_ARRAY, elements, Nil)
  }
}
