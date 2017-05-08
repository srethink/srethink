package srethink.ast

import srethink.json._
import srethink.net._
import srethink._
import scala.concurrent.Future


trait AstDef[J, F[_]] extends RethinkOp[J, F] with Models {

  import Helpers._

  object r {
    def db(name: String) = new Database(name)
    def dbCreate(name: String) = new EndAst(rDBCreate(name))
    def dbDrop(name: String) = new EndAst(rDBDrop(name))
    def epoch(date: java.util.Date): J = encode[java.util.Date](date)
    def now() = epoch(new java.util.Date())
    def epoch(time: Long): J = epoch(new java.util.Date(time))
    def obj(fields: (String, Expr)*): J = {
      jsObject(fields.map {
        case (k, v) => k -> v.term
      })
    }
    def asc(field: String) = new ROrder(rAsc(field))
    def desc(field: String) = new ROrder(rDesc(field))
  }

  trait Ast {
    val term: J

    def run(implicit executor: QueryExecutor) = {
      exec(term)
    }

    def runAs[T: F](implicit executor: QueryExecutor) = {
      decodeR[T](run)
    }

    def cursor[T: F](implicit executor: QueryExecutor) = {
      decodeStream[T](execCursor(term))
    }
  }

  trait WithUpdate { this: Ast =>
    def update(fields: (String, J)*) = {
      new EndAst(rUpdate(term, jsObject(fields)))
    }
  }

  trait ExprOps {
    val term: J
    def + (that: Expr) = new Expr(rAdd(term, that.term))
    def - (that: Expr) = new Expr(rSub(term, that.term))
    def * (that: Expr) = new Expr(rMul(term, that.term))
    def / (that: Expr) = new Expr(rDiv(term, that.term))
    def === (that: Expr) = new Expr(rEq(term, that.term))
    def =!= (that: Expr) = new Expr(rNe(term, that.term))
    def > (that: Expr) = new Expr(rGt(term, that.term))
    def < (that: Expr) = new Expr(rLt(term, that.term))
    def >= (that: Expr) = new Expr(rGe(term, that.term))
    def <= (that: Expr) = new Expr(rLe(term, that.term))
    def count() = new Expr(rCount(term))
    def nth(i: Int) =  new Expr(rNth(term, i))
    def max(field: String)= new Expr(rMax(term, field))
    def default(that: Expr) = new Expr(rDefault(term, that.term))
    def selectDynamic(field: String) = {
      new Expr(rGetField(term, field))
    }
  }

  class Var(val term: J) extends Dynamic with Ast with ExprOps {
  }

  class EndAst(val term: J) extends Ast

  implicit class Expr(val term: J) extends Ast with ExprOps with Dynamic{

  }

  class Selection(val term: J) extends Ast with WithUpdate {
    def map(f: Var => Expr) = {
      val doc = rVar(1)
      val func = rFunc(1, f(new Var(doc)).term)
      new Selection(rMap(term, func))
    }

    def max(field: String)= new Selection(rMax(term, field))

    def delete(options: (String, J)*) = {
      new EndAst(rDelete(term, jsObject(options)))
    }

    def count() = {
      new Selection(rCount(term))
    }

    def group(f: Var => Expr) = {
      val field = Left(toJ(f))
      new Selection(rGroup(term, field))
    }

    def groupByIndex(index: String) = {
      val option: Seq[(String, J)] = Seq("index" -> index)
      new Selection(rGroup(term, Right(option)))
    }

    def group(field: String) = {
      new Selection(rGroup(term, Left(field)))
    }

    def ungroup() = {
      new Selection(rUnGroup(term))
    }

    def skip(n: Long) = {
      new Selection(rSkip(term, n))
    }

    def filter(f: Var => Expr) = {
      val funcJ = toJ(f)
      new Selection(rFilter(term, funcJ))
    }

    def limit(n: Int) = {
      new Selection(rLimit(term, n))
    }

    def orderBy(order: ROrder*) = new Selection(rOrderBy(term, order.map(_.term)))
    def orderByIndex(order: ROrder) = new Selection(rOrderByOption(term, Seq("index" -> order.term)))
  }

  class Database(dbName: String) extends Ast {
    val term = rDatabase(dbName)

    def table(name: String, options: (String, J)*) = new Table(term, name, options: _*)

    def tableCreate(name: String, options: (String, J)*) =
      new EndAst(rTableCreate(term, name, jsObject(options)))

    def tableDrop(name: String) = new EndAst(rTableDrop(term, name))
  }

  class ROrder(j: J) extends Ast {
    val term = j
  }

  class Table(
    db: J,
    name: String,
    opt: (String, J)*)
      extends Selection(rTable(db, name, jsObject(opt))) with  Ast {

    def get[K: F](key: K, opt: (String, J)*) = {
      new Selection(rGet(term, transformArray(encode[K](key)), jsObject(opt)))
    }

    def getAll[K: F](keys: Seq[K], opt: (String, J)*) = {
      val jsKeys = keys.map(k => transformArray(encode[K](k)))
      new Selection(rGetAll(term, jsKeys, jsObject(opt)))
    }

    def between[K: F](lower: K, upper: K, opt: (String, J)*) = {
      val lowerJ = transformArray(encode[K](lower))
      val upperJ = transformArray(encode[K](upper))
      new Selection(rBetween(term, lowerJ, upperJ, jsObject(opt)))
    }

    def insert[A: F](docs: Seq[A], opts: (String, J)*) = {
      val datas =docs.map(encode[A])
      insertJS(datas, opts: _*)
    }

    def insertJS(docs: Seq[J], opts: (String, J)*) = {
      new EndAst(rInsert(term, docs.map(transformArray), jsObject(opts)))
    }

    def indexCreate(name: String)(f: Var => Expr) = {
      new EndAst(rIndexCreate(term, name, toJ(f)))
    }

    def indexDrop(name: String) = {
      new EndAst(rIndexDrop(term, name))
    }
  }

  implicit def booleanAsExpr(b: Boolean) = new Expr(b)
  implicit def longAsExpr(i: Long): Expr = new Expr(i)
  implicit def stringAsExpr(s: String): Expr = new Expr(s)
  implicit def doubleAsExpr(d: Double): Expr = new Expr(d)

  private object Helpers {
    def toJ(f: Var => Expr) = {
      val arg = rVar(1)
      val body = f(new Var(arg))
      rFunc(1, body.term)
    }
  }
}
