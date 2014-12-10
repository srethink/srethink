package srethink.ast.ops

import scala.concurrent.Future
import srethink.json._
import srethink.net._
import srethink.ast._

trait ROptions extends JsonDef {
  object o {
    trait RethinkOption {
      private[ops] val name: String
      private[ops] var value: Option[JsValue]
    }

    class ROption[T] private[ops](private[ops] val name: String) extends RethinkOption {
      private[ops] var value = None: Option[JsValue]
      final def apply(v: T): this.type = {
        value = Some(
          v match {
            case o : Int => jsNumber(o)
            case o : Boolean => jsBool(o)
            case o : String => jsString(o)
          }
        )
        this
      }
    }

    private[ops] def options(options: Seq[RethinkOption]) = {
      val opts = options.collect {
        case k: ROption[_] if k.value.isDefined => k.name -> k.value.get
      }
      jsObject(opts)
    }

    trait RInsertOption extends RethinkOption
    trait RTableOption extends RethinkOption
    trait RTableCreateOption extends RethinkOption
    trait RDeleteOption extends RethinkOption
    trait RGetAllOption extends RethinkOption

    val durability = new ROption[String]("durability")
        with RInsertOption
        with RTableOption
        with RDeleteOption{
      def soft() = apply("soft")
      def hard() = apply("hard")
    }
    val returnVals = new ROption[Boolean]("return_changes") with RInsertOption
    val conflict = new ROption[String]("conflict") with RInsertOption {
      def overwrites() = apply("overwrites")
      def update() = apply("updates")
      def error() = apply("error")
    }
    val userOutdate = new ROption[Boolean]("use_outdated") with RTableOption
    val primaryKey = new ROption[String]("primary_key") with RTableCreateOption
    val datacenter = new ROption[String]("datacenter")
    val index = new ROption[String]("index")
  }
}

private[ast] trait RethinkOp extends ROptions with JsonDef with Terms with ResultDecoders {

  import srethink.protocol.ResponseConstant._
  private val SUCCESS_ATOM = jsNumber(SUCCESS_ATOM_VALUE)
  private val SUCCESS_SEQUENCE = jsNumber(SUCCESS_SEQUENCE_VALUE)
  private val SUCCESS_PARTIAL = jsNumber(SUCCESS_PARTIAL_VALUE)
  private val SUCCESS_FEED = jsNumber(SUCCESS_FEED_VALUE)
  private val WAIT_COMPLETE = jsNumber(WAIT_COMPLETE_VALUE)
  private val CLIENT_ERROR = jsNumber(CLIENT_ERROR_VALUE)
  private val COMPILE_ERROR = jsNumber(COMPILE_ERROR_VALUE)
  private val RUNTIME_ERROR = jsNumber(RUNTIME_ERROR_VALUE)

  private val rethinkErrorHandler: (Response => (Long, JsValue, JsArray)) = { r =>
    val fields = unapplyJsObject(parse(r.body).asInstanceOf[JsObject])
    val Some(rt) = fields.collectFirst {
      case (n, v) if n == "t" => v
    }
    val Some(rr) = fields.collectFirst {
      case (n, v) if n == "r" => v.asInstanceOf[JsArray]
    }
    rt match {
      case CLIENT_ERROR | COMPILE_ERROR | RUNTIME_ERROR =>
        throw new RethinkException(stringify(rr))
      case _ =>
        (r.token, rt, rr)
    }
  }

  private def startQuery(query: JsValue)(implicit executor: QueryExecutor) = {
    import executor.executionContext
    executor.start(stringify(rStartQuery(query))).map(rethinkErrorHandler)
  }

  protected def sequence(query: JsValue)(implicit executor: QueryExecutor) = {
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

  protected def atom(query: JsValue)(implicit executor: QueryExecutor) = {
    import executor.executionContext
    startQuery(query).map {
      case (t, SUCCESS_ATOM, body) =>
        unapplyJsArray(body)(0)
      case (t, rt, body) =>
        throw new RethinkException(s"wrong response type, expected: ${SUCCESS_SEQUENCE}, actual: ${rt}")
    }
  }

  def decodeR[T: JsDecoder](r: Future[JsValue])(implicit executor: QueryExecutor) = {
    import executor.executionContext
    r.map(decode[T])
  }
}
