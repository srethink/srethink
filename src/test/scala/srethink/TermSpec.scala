package srethink

import org.specs2.execute.AsResult
import org.specs2.mutable.Specification
import org.specs2.specification._
import org.specs2.matcher.MatchResult
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import srethink.api._
import srethink.ast._
import srethink.protocol._
import srethink.protocol.Response.ResponseType._
import AstHelper._

trait TermQuery extends Connected with TokenGenerator {

  def db(dbName: String) = {
    strTerm(dbName)
  }

  def tb(name: String) = {
    strTerm(name)
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
    query(term).map{matcher}.await(100)
  }

  def expectSuccessAtom(term: RTerm) = expect(term)(_.`type`.get === Response.ResponseType.SUCCESS_ATOM)

  def expectNotNull(term: RTerm) = expect(term)(_.response(0).`type`.get !== Datum.DatumType.R_NULL)

  def expectNull(term: RTerm) = expect(term) {
    case Response(Some(tpe), Some(token), response, backtrace, profile) =>
      tpe must be_==(SUCCESS_ATOM) or be_==(SUCCESS_SEQUENCE) or be_==(SUCCESS_PARTIAL)
      response(0).`type`.get === Datum.DatumType.R_NULL
  }

}

trait TermSpec extends Specification with TermQueryMatchers {
  import scala.concurrent._

  sequential // disable parallel execution



  override def map(fs: =>Fragments) = Step(connect())  ^ fs ^ Step(disconnect())
}

trait WithTestDatabase extends TermSpec with BeforeAfterExample {

  val database = new RDb("test")

  def before = {
    ready(DBCreate(db("test")))
  }

  def after = {
    ready(DBDrop(db("test")))
  }
}

trait WithTestTable extends WithTestDatabase {
  val table = RTable("test",
    rdb = Some(database))

  val opts = RTermOpts("primary_key" -> strTerm("id"))

  override def before = {
    super.before
    ready(TableCreate(tb("test"), opts = opts))
  }

  override def after = {
    ready(TableDrop(tb("test")))
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
    ready(Insert(table, new DatumTerm(testDoc) :: Nil))
  }

}
