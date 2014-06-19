package srethink.ast

import org.specs2.execute.AsResult
import org.specs2.mutable.Specification
import org.specs2.specification._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import srethink.protocol._
import srethink._


trait TermQuery extends Connected with TokenGenerator {

  def db(dbName: String) = {
    DatumTerm(RStr(dbName))
  }

  def tb(name: String) = {
    DatumTerm(RStr(name))
  }

  def query(term: RTerm) = {
    val q = Query(
      `type` = Some(Query.QueryType.START),
      query = Some(term.toTerm),
      token = Some(nextToken)
    )
    connection.query(q).map {
      case Response(tpe, token, resp, backtrace, profile) =>
        tpe
    }
  }
}

trait TermQueryMatchers extends  TermQuery {
  this: Specification =>
  def expect(term: RTerm, resp: Response.ResponseType.EnumVal) = {
    Await.result(query(term), duration.Duration.Inf) must beSome(resp)
  }
  def expectSuccessAtom(term: RTerm) = expect(term, Response.ResponseType.SUCCESS_ATOM)
}

trait TermSpec extends Specification with TermQueryMatchers {
  import scala.concurrent._

  sequential // disable parallel execution

  def ready(q: RTerm) = {
    Await.result(query(q), duration.Duration.Inf)
  }

  override def map(fs: =>Fragments) = Step(connect())  ^ fs ^ Step(disconnect())
}

trait WithTestDatabase extends TermSpec with BeforeAfterExample {

  val database = RDb(DatumTerm(RStr("test")))

  def before = {
    println("creating test db")
    ready(DBCreate(db("test")))
  }

  def after = {
    println("dropping test db")
    ready(DBDrop(db("test")))
  }
}

trait WithTestTable extends WithTestDatabase {
  val table = RTable(tb("test"))

  override def before = {
    super.before
    println("creating test table")
    ready(TableCreate(tb("test")))
  }

  override def after = {
    println("dropping test table")
    ready(TableDrop(tb("test")))
    super.after
  }

}
