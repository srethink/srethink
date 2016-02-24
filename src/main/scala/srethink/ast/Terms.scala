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

  def rInsert(table: J, values: Seq[J], options: J = EmptyOpts) = {
    rTerm(INSERT_VALUE, Seq(table, rArray(values)), options)
  }

  def rTable(db: J, name: String, options: J = EmptyOpts): J = {
    rTerm(TABLE_VALUE, Seq(db, name), options)
  }

  def rDatabase(name: String) = {
    rTerm(DB_VALUE, Seq(name))
  }

  def rTableCreate(database: J, name: String,  options: J = EmptyOpts) = {
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

  def rDelete(selection: J, options: J = EmptyOpts) = {
    rTerm(DELETE_VALUE, Seq(selection), options)
  }

  def rGetAll(table: J,keys: Seq[J], options: J = EmptyOpts) = {
    rTerm(GET_ALL_VALUE, table +: keys, options )
  }

  def rGet(table: J,key: J, options: J = EmptyOpts) = {
    rTerm(GET_VALUE, table +: key +: Nil, options)
  }

  def rBetween(term: J, lower: J, upper: J, options: J = EmptyOpts) = {
    rTerm(BETWEEN_VALUE, Seq(term ,lower, upper), options)
  }

  def rFilter(term: J, func: J) = {
    rTerm(FILTER_VALUE, Seq(term, func))
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

  def rEq(left: J, right: J) = {
    rTerm(EQ_VALUE, Seq(left, right))
  }

  def rNe(left: J, right: J) = {
    rTerm(NE_VALUE, Seq(left, right))
  }

  def rGt(left: J, right: J) = {
    rTerm(GT_VALUE, Seq(left, right))
  }

  def rGe(left: J, right: J) = {
    rTerm(GE_VALUE, Seq(left, right))
  }

  def rLt(left: J, right: J) = {
    rTerm(LT_VALUE, Seq(left, right))
  }

  def rLe(left: J, right: J) = {
    rTerm(LE_VALUE, Seq(left, right))
  }

  def rNth(term: J, n: Int) = {
    rTerm(NTH_VALUE, Seq(term, jsNumber(n)))
  }

  def rGroup(selection: J, fieldOrOption: Either[J, J]) = {
    fieldOrOption match {
      case Left(field) =>
        rTerm(GROUP_VALUE, Seq(selection, field))
      case Right(option) =>
        rTerm(GROUP_VALUE, Seq(selection), option)
    }
  }

  def rUnGroup(term: J) = {
    rTerm(UNGROUP_VALUE, Seq(term))
  }

  def rCount(term: J) = {
    rTerm(COUNT_VALUE, Seq(term))
  }

  def rSkip(term: J, count: Long) = {
    rTerm(SKIP_VALUE, Seq(term, count))
  }

  def rLimit(term: J, count: Int) = {
    rTerm(LIMIT_VALUE, Seq(term, jsNumber(count)))
  }

  def rUpdate(term: J, fields: J, options: J = EmptyOpts) = {
    rTerm(UPDATE_VALUE, Seq(term, fields), options)
  }

  def rMax(term: J, field: String) = {
    rTerm(MAX_VALUE, Seq(term, field))
  }

  def rMax(term: J) = {
    rTerm(MAX_VALUE, Seq(term))
  }

  def rAsc(field: String) = {
    rTerm(ASC_VALUE, Seq(field))
  }

  def rOrderBy(term: J, order: Seq[J]) = {
    rTerm(ORDERBY_VALUE, term +: order)
  }

  def rDesc(field: String) = {
    rTerm(DESC_VALUE, Seq(field))
  }
}
