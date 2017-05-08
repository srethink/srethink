package srethink.ast

import fs2._
import fs2.util._
import scala.concurrent._
import srethink.json._
import srethink.net._
import srethink.ast._
import srethink._
import org.slf4j._

import scala.util.Try

private[ast] trait RethinkOp[J, F[_]] extends Terms[J, F]  {

  private val logger = LoggerFactory.getLogger("srethink.ast.exec")

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

  private def repeatEvalFuture[A](f: => Future[A]): Stream[Future, A] = {
    Stream.eval(f) ++ repeatEvalFuture(f)
  }

  protected def execCursor(query: J)(implicit executor: QueryExecutor): Stream[Future, J] = {
    val (c, t) = executor.prepare()
    (Stream.eval(startQuery(c, t, query)) ++ repeatEvalFuture(continue(c, t))).takeThrough {
      case (t, rt, body) => rt == SUCCESS_PARTIAL
    }.flatMap {
      case (_, rt, body) =>
        val docs = normalizeResult(rt, body)
        if(logger.isInfoEnabled)
          logger.info(s"Getting docs ${rt} -> ${docs.size}")
        Stream.emits(docs)
    }
  }

  private def startQuery(c: Connection, t: Long, query: J)(implicit executor: QueryExecutor) = {
    import executor.executionContext
    executor.start(c, t, stringify(rStartQuery(query))).map(rethinkErrorHandler)
  }

  private def continue(c: Connection, t: Long)(implicit executor: QueryExecutor) = {
    import executor.executionContext
    println(s"cintinue $t")
    executor.start(c, t, stringify(rContinueQuery())).map(rethinkErrorHandler)
  }

  protected def exec(query: J)(implicit executor: QueryExecutor): Future[J] = {
    import executor.executionContext
    val (c, t) = executor.prepare()
    startQuery(c, t, query).map {
      case (token, SUCCESS_SEQUENCE, body) =>
        unapplyJsArray(body)
      case (token, SUCCESS_PARTIAL, body) =>
        executor.stop(c, t, stringify(rStopQuery()))
        unapplyJsArray(body)
      case (t, SUCCESS_ATOM, body) =>
        unapplyJsArray(body)(0)
      case (t, rt, body) =>
        throw new RethinkException(s"wrong response type, expected: ${SUCCESS_SEQUENCE}, actual: ${rt}")

    }
  }

  private def normalizeResult(rt: J, body: J): Seq[J] = rt match {
    case SUCCESS_SEQUENCE => unapplyJsArray(body)
    case SUCCESS_PARTIAL => unapplyJsArray(body)
    case SUCCESS_ATOM => unapplyJsArray(unapplyJsArray(body)(0))
    case _ => throw new RethinkException(s"wrong response type: ${rt}")
  }

  def decodeR[T: F](r: Future[J])(implicit executor: QueryExecutor) = {
    import executor.executionContext
    r.map(decode[T]).recoverWith {
      case _ => r.flatMap { body =>
        Future.failed(new RethinkException(s"error parsing body ${stringify(body)}"))
      }
    }
  }

  def decodeStream[T: F](r: Stream[Future, J])(implicit executor: QueryExecutor): Stream[Future, T] = {
    import executor.executionContext
    r.flatMap { body =>
     val fut: Future[T] =  Future.fromTry(Try(decode[T](body))).recover {
        case ex: Throwable =>  throw new RethinkException(s"error parsing body ${stringify(body)}")
     }
      Stream.eval(fut)
    }
  }

}
