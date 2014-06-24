package srethink.ast

import org.specs2.execute.AsResult
import org.specs2.mutable.Specification
import org.specs2.specification._
import org.specs2.matcher.MatchResult
import scala.collection.immutable.Seq
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
    connection.query(q)
  }

  def ready(q: RTerm) = {
    Await.result(query(q), duration.Duration.Inf)
  }
}

trait TermQueryMatchers extends  TermQuery {
  this: Specification =>

  def expect[T](term: RTerm)(matcher: Response => MatchResult[T]) = {
    query(term).map{ r => println(r); matcher(r) }.await(100)
  }

  def expectSuccessAtom(term: RTerm) = expect(term)(_.`type`.get === Response.ResponseType.SUCCESS_ATOM)

  def expectNotNull(term: RTerm) = expect(term)(_.response(0).`type`.get !== Datum.DatumType.R_NULL)

  def expectNull(term: RTerm) = expect(term) {
    case Response(Some(tpe), Some(token), response, Some(backtrace), Some(profile)) =>
      tpe === Response.ResponseType.SUCCESS_SEQUENCE
      response(0).`type`.get === Datum.DatumType.R_NULL
  }

}

trait TermSpec extends Specification with TermQueryMatchers {
  import scala.concurrent._

  sequential // disable parallel execution



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
  val table = RTable(
    tb("test"),
    rdb = Some(database))

  val opts = RTermOpts("primary_key" -> DatumTerm(RStr("id")))

  override def before = {
    super.before
    println("creating test table")
    ready(TableCreate(tb("test"), opts = opts))
  }

  override def after = {
    println("dropping test table")
    ready(TableDrop(tb("test")))
    super.after
  }

  trait WithTestData extends Scope {
    val testDoc = RObject(
      Seq(
        "id" -> RNum(1),
        "name" -> RStr("foo"),
        "age" -> RNum(100)
      )
    )
    ready(Insert(table, DatumTerm(testDoc)))
  }

}
