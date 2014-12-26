package srethink.ast

import srethink.json._
import srethink.protocol.TermConstant._
import srethink.protocol.QueryConstant._

private[ast] trait Terms { this: JsonDef =>

  val EmptyOpts = jsObject(Seq.empty)

  def rQuery(queryType: Int, term: JsValue, optargs: JsObject = EmptyOpts) = {
    jsArray(Seq(jsNumber(queryType), term, optargs))
  }

  def rQuery(queryType: Int) = {
    jsArray(Seq(jsNumber(queryType)))
  }

  def rStartQuery(term: JsValue, optargs: JsObject = EmptyOpts) = {
    rQuery(START_VALUE, term, optargs)
  }

  def rStopQuery() = {
    rQuery(STOP_VALUE)
  }

  def rTerm(tpe: Int, args: Seq[JsValue], optargs: JsObject = EmptyOpts) = {
    if(optargs == EmptyOpts) {
      jsArray(Seq(jsNumber(tpe), args))
    } else {
      jsArray(Seq(jsNumber(tpe), args, optargs))
    }
  }

  def rVar(id: Int) = {
    rTerm(VAR_VALUE, Seq(jsNumber(id)))
  }

  def rArray(values: Seq[JsValue]) = {
    rTerm(MAKE_ARRAY_VALUE, values)
  }

  def rFunc(argc: Int, body: JsValue) = {
    val vars = rArray((1 to argc).map(jsNumber(_)))
    rTerm(FUNC_VALUE, Seq(vars, body))
  }

  def rInsert(table: JsValue, values: Seq[JsValue], options: JsObject) = {
    rTerm(INSERT_VALUE, Seq(table, rArray(values)), options)
  }

  def rTable(db: JsValue, name: String, options: JsObject): JsValue = {
    rTerm(TABLE_VALUE, Seq(db, name), options)
  }

  def rDatabase(name: String) = {
    rTerm(DB_VALUE, Seq(name))
  }

  def rTableCreate(database: JsValue, name: String,  options: JsObject) = {
    rTerm(TABLE_CREATE_VALUE, Seq(database, name), options)
  }

  def rTableDrop(database: JsValue, table: String) = {
    rTerm(TABLE_DROP_VALUE, Seq(database, table))
  }

  def rDBCreate(db: String) = {
    rTerm(DB_CREATE_VALUE, Seq(db))
  }

  def rDBDrop(db: String) = {
    rTerm(DB_DROP_VALUE, Seq(db))
  }

  def rDelete(selection: JsValue, options: JsObject) = {
    rTerm(DELETE_VALUE, Seq(selection), options)
  }

  def rGetAll(table: JsValue,keys: Seq[JsValue], options: JsObject) = {
    rTerm(GET_ALL_VALUE, table +: keys, options )
  }

  def rGet(table: JsValue,key: JsValue, options: JsObject) = {
    rTerm(GET_VALUE, table +: key +: Nil, options )
  }

  def rGetField(obj: JsValue, field: String) = {
    rTerm(GET_FIELD_VALUE, Seq(obj, field))
  }

  def rIndexCreate(table: JsValue, name: String, func: JsValue) = {
    rTerm(INDEX_CREATE_VALUE, Seq(table, name, func))
  }

  def rIndexDrop(table: JsValue, name: String) = {
    rTerm(INDEX_DROP_VALUE, Seq(table, name))
  }

  def rMap(term: JsValue, func: JsValue) = {
    rTerm(MAP_VALUE, Seq(term, func))
  }

  def rAdd(left: JsValue, right: JsValue) = {
    rTerm(ADD_VALUE, Seq(left, right))
  }

  def rSub(left:JsValue, right: JsValue) = {
    rTerm(SUB_VALUE, Seq(left, right))
  }

  def rMul(left: JsValue, right: JsValue) = {
    rTerm(MUL_VALUE, Seq(left, right))
  }

  def rDiv(left: JsValue, right: JsValue) = {
    rTerm(DIV_VALUE, Seq(left, right))
  }
}
