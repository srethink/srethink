package srethink

import org.specs2.execute.AsResult
import org.specs2.mutable.Specification
import org.specs2.specification._
import org.specs2.matcher.MatchResult
import scala.concurrent._
import srethink.api._
import srethink.protocol._
import srethink.protocol.Response.ResponseType._
import AstHelper._

trait TermQuery extends Connected  {

  def query(term: RTerm) = {
    queryExecutor.query(term)
  }

  def ready(q: RTerm) = {
    try {
      Await.result(query(q), duration.Duration.Inf)
    } catch {
      case e: Exception =>
        e.printStackTrace

    }
  }
}

trait TermQueryMatchers extends  TermQuery {
  this: Specification =>

  def expect[T](term: RTerm)(matcher: QuerySuccess => MatchResult[T]) = {
    query(term).map{matcher}.await(1000)
  }

  def expectSuccessAtom(term: RTerm) = expect(term)(_.successType === Response.ResponseType.SUCCESS_ATOM)

  def expectNotNull(term: RTerm) = expect(term)(_.data(0).`type`.get !== Datum.DatumType.R_NULL)

  def expectNull(term: RTerm) = expect(term) {
    case QuerySuccess(successType, data) =>
      data(0).`type`.get === Datum.DatumType.R_NULL
  }

}

trait TermSpec extends org.specs2.mutable.Specification with TermQueryMatchers {
  sequential // disable parallel execution
  override def map(fs: =>Fragments) = fs ^ Step(disconnect())
}

trait WithTestDatabase extends TermSpec with BeforeAfterExample {

  def tableName = "test_tb"

  def databaseName = "test_db"

  def database = new RDb(databaseName)

  def before = {
    ready(DBCreate(databaseName))
  }

  def after = {
    ready(DBDrop(databaseName))
  }
}

trait WithTestTable extends WithTestDatabase {
  val table = RTable(tableName, database)

  val opts = RTermOpts("primary_key" -> strTerm("id"))

  override def before = {
    println("creating test database")
    super.before
    println("creating test table")
    ready(TableCreate(database, tableName, opts = opts))
  }

  override def after = {
    println("dropping test table")
    ready(TableDrop(database, tableName))
    println("dropping test database")
    super.after
  }
}
