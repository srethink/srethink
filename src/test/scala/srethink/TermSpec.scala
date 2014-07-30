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

  def tableName = "test"

  def database = new RDb(tableName)

  def before = {
    ready(DBCreate(tableName))
  }

  def after = {
    ready(DBDrop(tableName))
  }
}

trait WithTestTable extends WithTestDatabase {
  val table = RTable(tableName,
    rdb = Some(database))

  val opts = RTermOpts("primary_key" -> strTerm("id"))

  override def before = {
    println("creating test database")
    super.before
    println("creating test table")
    ready(TableCreate(tableName, opts = opts))
  }

  override def after = {
    println("dropping test table")
    ready(TableDrop(tableName))
    println("dropping test database")
    super.after
  }

  trait WithTestData extends Scope {
    val testDoc = new RObject(
      Seq(
        "id" -> new RNum(1),
        "name" -> new RStr("foo"),
        "age" -> new RNum(100)
      )
    )
    ready(Insert(table, testDoc :: Nil))
  }

}
