package srethink.ast

import fs2._
import cats._
import cats.effect._
import cats.syntax.either._
import scala.concurrent._
import srethink.json._
import srethink.net._
import srethink.ast._
import srethink._
import org.slf4j._

import scala.util.Try

private[ast] trait RethinkOp[J, F[_]] extends Terms[J, F] {

  private val logger = LoggerFactory.getLogger("srethink.ast.exec")

  import srethink.protocol.ResponseConstant._
  private val SUCCESS_ATOM     = jsNumber(SUCCESS_ATOM_VALUE)
  private val SUCCESS_SEQUENCE = jsNumber(SUCCESS_SEQUENCE_VALUE)
  private val SUCCESS_PARTIAL  = jsNumber(SUCCESS_PARTIAL_VALUE)
  private val SUCCESS_FEED     = jsNumber(SUCCESS_FEED_VALUE)
  private val WAIT_COMPLETE    = jsNumber(WAIT_COMPLETE_VALUE)
  private val CLIENT_ERROR     = jsNumber(CLIENT_ERROR_VALUE)
  private val COMPILE_ERROR    = jsNumber(COMPILE_ERROR_VALUE)
  private val RUNTIME_ERROR    = jsNumber(RUNTIME_ERROR_VALUE)

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

  private def repeatEvalFuture[A](
    f: => Future[A]
  )(implicit executor: QueryExecutor): Stream[IO, A] = {
    Stream.eval(IO.fromFuture(IO(f))).repeat
  }

  protected def execCursor(
    query: J
  )(implicit executor: QueryExecutor): Stream[IO, J] = {
    import executor.executionContext
    val (c, t) = executor.prepare()
    val start  = IO.fromFuture(IO.delay(startQuery(c, t, query)))
    val rows = Stream.eval(start).flatMap {
      case (t, initRt, docs) =>
        Stream
          .chunk(Chunk.from(normalizeResult(initRt, docs)))
          .covary[IO] ++ Stream
          .unfoldChunkEval(initRt) { rt =>
            val hasMore = rt == SUCCESS_PARTIAL
            if (hasMore) {
              IO.fromFuture(IO.delay(continue(c, t))).map {
                case (_, nrt, ndocs) =>
                  Some(Chunk.from(normalizeResult(nrt, ndocs)) -> nrt)
              }
            } else IO.pure(None)
          }
    }

    val stopEval = IO.delay(
      stop(c, t).recover {
        case ex: Throwable =>
          logger.debug(s"Failed close cursor for token $t")
      }
        .map(_ => {})
    )
    rows.onFinalize(IO.fromFuture(stopEval))
  }

  private def startQuery(c: Connection, t: Long, query: J)(implicit
    executor: QueryExecutor
  ) = {
    import executor.executionContext
    logger.info(s"[RethinkOp] start Query, token: ${t}")
    executor.start(c, t, stringify(rStartQuery(query))).map(rethinkErrorHandler)
  }

  private def continue(c: Connection, t: Long)(implicit
    executor: QueryExecutor
  ) = {
    logger.info(s"[RethinkOp] continue Query, token: ${t}")
    import executor.executionContext
    executor
      .continue(c, t, stringify(rContinueQuery()))
      .map(rethinkErrorHandler)
  }

  private def stop(c: Connection, t: Long)(implicit executor: QueryExecutor) = {
    import executor.executionContext
    executor.start(c, t, stringify(rStopQuery())).map(rethinkErrorHandler)
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
        throw new RethinkException(
          s"wrong response type, expected: ${SUCCESS_SEQUENCE}, actual: ${rt}"
        )

    }
  }

  private def normalizeResult(rt: J, body: J): Seq[J] = rt match {
    case SUCCESS_SEQUENCE => unapplyJsArray(body)
    case SUCCESS_PARTIAL  => unapplyJsArray(body)
    case SUCCESS_ATOM     => unapplyJsArray(unapplyJsArray(body)(0))
    case _ => throw new RethinkException(s"wrong response type: ${rt}")
  }

  def decodeR[T: F](r: Future[J])(implicit executor: QueryExecutor) = {
    import executor.executionContext
    r.map(decode[T]).recoverWith {
      case ex =>
        r.flatMap { body =>
          logger.info(s"error decode body", ex)
          Future.failed(
            new RethinkException(s"error parsing body ${stringify(body)}")
          )
        }
    }
  }

  def decodeStream[T: F](
    r: Stream[IO, J]
  )(implicit executor: QueryExecutor): Stream[IO, T] = {
    import executor.executionContext
    r.evalMapChunk { body =>
      IO.fromEither(Either.fromTry(Try(decode[T](body))))
    }
  }

}
