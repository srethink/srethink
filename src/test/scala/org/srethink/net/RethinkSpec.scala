package org.srethink.net

import cats.effect._
import org.slf4j._
import org.scalatest._
import org.srethink._
import scala.concurrent.ExecutionContext.global

trait RethinkSpec extends AsyncFlatSpec with Matchers with IOAsserts {
  lazy val rdb = Holder.rdb.unsafeRunSync()
  implicit val timer = IO.timer(global)
  implicit val cs = IO.contextShift(global)

  def ready[A](io: IO[A]) = io.unsafeRunSync()

  object Holder {
    val log = LoggerFactory.getLogger(classOf[RethinkSpec])
    log.info("Init rdb...")
    val rdb = r.executor[IO]()
    log.info(s"Created rdb instance $rdb")
  }

}

trait IOAsserts {
  implicit def ioAsFuture[A](io: IO[A]) = io.unsafeToFuture()
}
