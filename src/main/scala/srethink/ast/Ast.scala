package srethink.ast

import srethink.json._
import srethink.net._
import ops._


trait AstDef extends RethinkOp with Terms {

  object r {
    def db(name: String) = new Database(name)
    def dbCreate(name: String) = new EndAst(rDBCreate(name))
    def dbDrop(name: String) = new EndAst(rDBDrop(name))
  }

  trait Ast {
    val term: JsValue
    def runAs[T: JsDecoder](implicit executor: QueryExecutor) = {
      decodeR[T](atom(term))
    }

    def run(implicit executor: QueryExecutor) = {
      atom(term)
    }
  }

  class Var(val term: JsValue) extends Dynamic with Ast {
    def selectDynamic(field: String) = {
      new Expr(rGetField(term, field))
    }
  }

  class EndAst(val term: JsValue) extends Ast

  class Expr(val term: JsValue) extends Ast {
    def + (that: Expr) = new Expr(rAdd(term, that.term))
    def - (that: Expr) = new Expr(rSub(term, that.term))
    def * (that: Expr) = new Expr(rMul(term, that.term))
    def / (that: Expr) = new Expr(rDiv(term, that.term))
  }

  class Selection[T](val term: JsValue) extends Ast{
    def map(f: Var => Expr) = {
      val doc = rVar(1)
      val func = rFunc(1, f(new Var(doc)).term)
      new Selection(rMap(term, func))
    }

    def delete(options: (String, JsValue)*) = {
      new EndAst(rDelete(term, jsObject(options)))
    }
  }

  class Database(dbName: String) extends Ast {
    val term = rDatabase(dbName)

    def table(name: String, options: (String, JsValue)*) = new Table(term, name, options: _*)

    def tableCreate(name: String, options: (String, JsValue)*) = new EndAst(rTableCreate(term, name, jsObject(options)))
  }

  class Table(
    db: JsValue,
    name: String,
    opt: (String, JsValue)*)
      extends Selection(rTable(db, name, jsObject(opt))) with  Ast {

    def get[K: JsEncoder](key: K, opt: (String, JsValue)*) = {
      new Selection(rGet(term, encode[K](key), jsObject(opt)))
    }

    def getAll[K: JsEncoder](keys: Seq[K], opt: (String, JsValue)*) = {
      val jsKeys = keys.map(encode[K])
      new Selection(rGetAll(term, jsKeys, jsObject(opt)))
    }

    def insert[A: JsEncoder](docs: Seq[A], opts: (String, JsValue)*) = {
      val datas =docs.map(encode[A])
      insertJS(datas, opts: _*)
    }

    def insertJS(docs: Seq[JsValue], opts: (String, JsValue)*) = {
      new EndAst(rInsert(term, docs, jsObject(opts)))
    }

    def indexCreate(name: String)(f: Var => Expr) = {
      val arg = rVar(1)
      val body = f(new Var(arg))
      val func = rFunc(1, body.term)
      new EndAst(rIndexCreate(term, name, func))
    }

    def indexDrop(name: String) = {
      new EndAst(rIndexDrop(term, name))
    }
  }

  implicit def longAsExpr(i: Long): Expr = new Expr(i)
  implicit def stringAsExpr(i: String): Expr = new Expr(i)
  implicit def doubleAsExpr(i: Double): Expr = new Expr(i)
}
