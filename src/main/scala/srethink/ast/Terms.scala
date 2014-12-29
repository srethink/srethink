package srethink.ast

import srethink.json._
import srethink.protocol.TermConstant._
import srethink.protocol.QueryConstant._

private[ast] trait Terms[J, F[_]] extends JsonDef[J, F] {

  val EmptyOpts = jsObject(Seq.empty)

  def rQuery(queryType: Int, term: J, optargs: J = EmptyOpts) = {
    jsArray(Seq(jsNumber(queryType), term, optargs))
  }

  def rQuery(queryType: Int) = {
    jsArray(Seq(jsNumber(queryType)))
  }

  def rStartQuery(term: J, optargs: J = EmptyOpts) = {
    rQuery(START_VALUE, term, optargs)
  }

  def rStopQuery() = {
    rQuery(STOP_VALUE)
  }

  def rTerm(tpe: Int, args: Seq[J], optargs: J = EmptyOpts) = {
    if(optargs == EmptyOpts) {
      jsArray(Seq(jsNumber(tpe), jsArray(args)))
    } else {
      jsArray(Seq(jsNumber(tpe), jsArray(args), optargs))
    }
  }

  def rVar(id: Int) = {
    rTerm(VAR_VALUE, Seq(jsNumber(id)))
  }

  def rArray(values: Seq[J]) = {
    rTerm(MAKE_ARRAY_VALUE, values)
  }

  def rFunc(argc: Int, body: J) = {
    val vars = rArray((1 to argc).map(jsNumber(_)))
    rTerm(FUNC_VALUE, Seq(vars, body))
  }

  def rInsert(table: J, values: Seq[J], options: J) = {
    rTerm(INSERT_VALUE, Seq(table, rArray(values)), options)
  }

  def rTable(db: J, name: String, options: J): J = {
    rTerm(TABLE_VALUE, Seq(db, name), options)
  }

  def rDatabase(name: String) = {
    rTerm(DB_VALUE, Seq(name))
  }

  def rTableCreate(database: J, name: String,  options: J) = {
    rTerm(TABLE_CREATE_VALUE, Seq(database, name), options)
  }

  def rTableDrop(database: J, table: String) = {
    rTerm(TABLE_DROP_VALUE, Seq(database, table))
  }

  def rDBCreate(db: String) = {
    rTerm(DB_CREATE_VALUE, Seq(db))
  }

  def rDBDrop(db: String) = {
    rTerm(DB_DROP_VALUE, Seq(db))
  }

  def rDelete(selection: J, options: J) = {
    rTerm(DELETE_VALUE, Seq(selection), options)
  }

  def rGetAll(table: J,keys: Seq[J], options: J) = {
    rTerm(GET_ALL_VALUE, table +: keys, options )
  }

  def rGet(table: J,key: J, options: J) = {
    rTerm(GET_VALUE, table +: key +: Nil, options )
  }

  def rGetField(obj: J, field: String) = {
    rTerm(GET_FIELD_VALUE, Seq(obj, field))
  }

  def rIndexCreate(table: J, name: String, func: J) = {
    rTerm(INDEX_CREATE_VALUE, Seq(table, name, func))
  }

  def rIndexDrop(table: J, name: String) = {
    rTerm(INDEX_DROP_VALUE, Seq(table, name))
  }

  def rMap(term: J, func: J) = {
    rTerm(MAP_VALUE, Seq(term, func))
  }

  def rAdd(left: J, right: J) = {
    rTerm(ADD_VALUE, Seq(left, right))
  }

  def rSub(left:J, right: J) = {
    rTerm(SUB_VALUE, Seq(left, right))
  }

  def rMul(left: J, right: J) = {
    rTerm(MUL_VALUE, Seq(left, right))
  }

  def rDiv(left: J, right: J) = {
    rTerm(DIV_VALUE, Seq(left, right))
  }
}
