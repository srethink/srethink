package srethink.ast

import org.specs2.execute.AsResult
import org.specs2.mutable.Specification
import org.specs2.specification.{BeforeAfterExample, Scope}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import srethink.protocol._
import srethink._


trait TermQuery extends Connected with TokenGenerator {

  def db(dbName: String) = {
    DatumTerm(RStr(dbName))
  }

  def query(term: RTerm) = {
    val q = Query(
      `type` = Some(Query.QueryType.START),
      query = Some(term.toTerm),
      token = Some(nextToken)
    )
    connection.query(q).map {
      case Response(tpe, token, resp, backtrace, profile) =>
        println(resp)
        tpe
    }
  }
}

trait TermQueryMatchers extends  TermQuery with BeforeAfterExample {
  this: Specification =>
  def expect(term: RTerm, resp: Response.ResponseType.EnumVal) = {
    Await.result(query(term), duration.Duration.Inf) must beSome(resp)
  }
  def expectSuccessAtom(term: RTerm) = expect(term, Response.ResponseType.SUCCESS_ATOM)
}

trait TermSpec extends Specification with TermQueryMatchers with BeforeAfterExample {
  override def before = {
    connect()
    Await.result(query(DBCreate(db("test"))), duration.Duration.Inf)
  }

  override def after = {
    Await.result(query(DBDrop(db("test"))), duration.Duration.Inf)
    disconnect()
  }
}

trait WithTestDatabase extends Specification  with TermQueryMatchers with BeforeAfterExample {
  val database = RDb(DatumTerm(RStr("test")))
  import scala.concurrent._

  override def before = {
    println("connecting...")
    connect()
    println("creating test db")
    Await.result(query(DBCreate(db("test"))), duration.Duration.Inf)
  }

  override def after = {
    println("dropping test db")
    Await.result(query(DBDrop(db("test"))), duration.Duration.Inf)
    println("disconnecting")
    disconnect()
  }
}
