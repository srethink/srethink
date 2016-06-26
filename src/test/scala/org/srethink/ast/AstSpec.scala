package org.srethink.ast

import org.srethink._
import org.scalatest._
import org.srethink.net.RethinkSpec
import scala.concurrent._

trait AstSpec extends RethinkSpec with BeforeAndAfterAll {

  def await[T](f: Future[T]) = Await.result(f, duration.Duration.Inf)
  def ready[T](f: Future[T]) = Await.ready(f, duration.Duration.Inf)
  val databaseName = "test_db"
  val tableName = "book"

  lazy val books = r.db(databaseName).table(tableName)

  override def beforeAll(): Unit = {
  }

  override def afterAll(): Unit = {

  }

  private def create() = {
    ready(rdb.run(r.dbCreate(databaseName)))
    ready(rdb.run(r.db(databaseName).tableCreate("book")))
  }

  private def drop() = {
    ready(rdb.run(r.dbDrop("test_db")))
    ready(rdb.run(r.db(databaseName).tableDrop("book")))
  }
}
