package srethink.ast

import cats.effect._
import play.api.rql._
import srethink._
import scala.concurrent.Future
import cats.effect.unsafe.implicits.global
import scala.concurrent.ExecutionContext

class CursorSpec extends RethinkSpec with WithData {

  implicit val ec: ExecutionContext = ExecutionContext.global

  private def insert10000() = {
    val all = (1 to 10000).map(i => book(i))
    all.grouped(1000).foldLeft(Future.successful({})) { (rs, bks) =>
      for {
        _ <- rs
        _ <- books.insert(bks).run
      } yield {}
    }
  }

  test("get all rows of table") {
    for {
      _  <- insert10000()
      rs <- books.cursor[Book].compile.toVector.unsafeToFuture()
    } yield assertEquals(rs.size, 10000)
  }

  test("get one row") {
    for {
      _  <- books.insert(Seq(book(1))).run
      rs <- books.cursor[Book].compile.toVector.unsafeToFuture()
    } yield assertEquals(rs.size, 1)
  }
}
