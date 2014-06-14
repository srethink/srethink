package srethink.ast

import srethink.protocol._
import srethink._
import scala.concurrent.ExecutionContext.Implicits.global

trait WithTermQuery extends WithConnection {

  def query(term: Term) = {
    val q = Query(
      `type` = Some(Query.QueryType.START),
      query = Some(term),
      token = Some(nextToken)
    )
    connection.query(q).map {
      case Response(tpe, token, resp, backtrace, profile) =>
        println(resp)
        tpe
    }
  }
}
