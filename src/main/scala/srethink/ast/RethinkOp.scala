package srethink.ast

import scala.concurrent.Future
import srethink.json._
import srethink.net._
import srethink.ast._
import srethink._

private[ast] trait RethinkOp[J, F[_]] extends Terms[J, F]  {

  import srethink.protocol.ResponseConstant._
  private val SUCCESS_ATOM = jsNumber(SUCCESS_ATOM_VALUE)
  private val SUCCESS_SEQUENCE = jsNumber(SUCCESS_SEQUENCE_VALUE)
  private val SUCCESS_PARTIAL = jsNumber(SUCCESS_PARTIAL_VALUE)
  private val SUCCESS_FEED = jsNumber(SUCCESS_FEED_VALUE)
  private val WAIT_COMPLETE = jsNumber(WAIT_COMPLETE_VALUE)
  private val CLIENT_ERROR = jsNumber(CLIENT_ERROR_VALUE)
  private val COMPILE_ERROR = jsNumber(COMPILE_ERROR_VALUE)
  private val RUNTIME_ERROR = jsNumber(RUNTIME_ERROR_VALUE)

  private val rethinkErrorHandler: (Response => (Long, J, J)) = { r =>
    val fields = unapplyJsObject(parse(r.body))
    val Some(rt) = fields.collectFirst {
      case (n, v) if n == "t" => v
    }
    val Some(rr) = fields.collectFirst {
      case (n, v) if n == "r" => v
    }
    rt match {
      case CLIENT_ERROR | COMPILE_ERROR | RUNTIME_ERROR =>
        throw new RethinkException(stringify(rr))
      case _ =>
        (r.token, rt, rr)
    }
  }

  private def startQuery(query: J)(implicit executor: QueryExecutor) = {
    import executor.executionContext
    executor.start(stringify(rStartQuery(query))).map(rethinkErrorHandler)
  }

  protected def sequence(query: J)(implicit executor: QueryExecutor) = {
    import executor.executionContext
    startQuery(query).map {
      case (token, SUCCESS_SEQUENCE, body) =>
        executor.complete(token)
        unapplyJsArray(body)
      case (token, SUCCESS_PARTIAL, body) =>
        executor.stop(token, stringify(rStopQuery()))
        throw new RethinkException(s"wrong response type, expected: ${SUCCESS_SEQUENCE}, actual: ${SUCCESS_PARTIAL}")
      case (t, rt, body) =>
        throw new RethinkException(s"wrong response type, expected: ${SUCCESS_SEQUENCE}, actual: ${rt}")

    }
  }

  protected def atom(query: J)(implicit executor: QueryExecutor) = {
    import executor.executionContext
    startQuery(query).map {
      case (t, SUCCESS_ATOM, body) =>
        unapplyJsArray(body)(0)
      case (t, rt, body) =>
        throw new RethinkException(s"wrong response type, expected: ${SUCCESS_SEQUENCE}, actual: ${rt}")
    }
  }

  def decodeR[T: F](r: Future[J])(implicit executor: QueryExecutor) = {
    import executor.executionContext
    r.map(decode[T])
  }
}
