package srethink.ast

import srethink.json._
import srethink.net._
import srethink._


trait AstDef[J, F[_]] extends RethinkOp[J, F] with Models {

  object r {
    def db(name: String) = new Database(name)
    def dbCreate(name: String) = new EndAst(rDBCreate(name))
    def dbDrop(name: String) = new EndAst(rDBDrop(name))
  }

  trait Ast {
    val term: J

    def run(implicit executor: QueryExecutor) = {
      atom(term)
    }

    def runAs[T: F](implicit executor: QueryExecutor) = {
      decodeR[T](run)
    }
  }

  class Var(val term: J) extends Dynamic with Ast {
    def selectDynamic(field: String) = {
      new Expr(rGetField(term, field))
    }
  }

  class EndAst(val term: J) extends Ast

  class Expr(val term: J) extends Ast {
    def + (that: Expr) = new Expr(rAdd(term, that.term))
    def - (that: Expr) = new Expr(rSub(term, that.term))
    def * (that: Expr) = new Expr(rMul(term, that.term))
    def / (that: Expr) = new Expr(rDiv(term, that.term))
  }

  class Selection[T](val term: J) extends Ast{
    def map(f: Var => Expr) = {
      val doc = rVar(1)
      val func = rFunc(1, f(new Var(doc)).term)
      new Selection(rMap(term, func))
    }

    def delete(options: (String, J)*) = {
      new EndAst(rDelete(term, jsObject(options)))
    }
  }

  class Database(dbName: String) extends Ast {
    val term = rDatabase(dbName)

    def table(name: String, options: (String, J)*) = new Table(term, name, options: _*)

    def tableCreate(name: String, options: (String, J)*) =
      new EndAst(rTableCreate(term, name, jsObject(options)))

    def tableDrop(name: String) = new EndAst(rTableDrop(term, name))
  }

  class Table(
    db: J,
    name: String,
    opt: (String, J)*)
      extends Selection(rTable(db, name, jsObject(opt))) with  Ast {

    def get[K: F](key: K, opt: (String, J)*) = {
      new Selection(rGet(term, encode[K](key), jsObject(opt)))
    }

    def getAll[K: F](keys: Seq[K], opt: (String, J)*) = {
      val jsKeys = keys.map(encode[K])
      new Selection(rGetAll(term, jsKeys, jsObject(opt)))
    }

    def insert[A: F](docs: Seq[A], opts: (String, J)*) = {
      val datas =docs.map(encode[A])
      insertJS(datas, opts: _*)
    }

    def insertJS(docs: Seq[J], opts: (String, J)*) = {
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
