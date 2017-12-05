package srethink.ast

import cats.effect._
import play.api.rql._
import srethink._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CursorSpec extends RethinkSpec with WithData {

  private def insert10000() = {
    val all = (1 to 10000).map(i => book(i))
    all.grouped(1000).foldLeft(Future.successful({})) { (rs, bks) =>
      for {
        _ <- rs
        _ <- books.insert(bks).run
      } yield {}
    }
  }

  "cursor" should {
    "get all rows of table" in {
      (for {
        _ <- insert10000()
        rs <- books.cursor[Book].runLog.unsafeToFuture
      } yield rs.size should be_== (10000)).await(100000)
    }

    "get one row" in {
      (for {
        _ <- books.insert(Seq(book(1))).run
        rs <- books.cursor[Book].runLog.unsafeToFuture
      } yield rs should have size(1)).await(100000)
    }
  }
}
