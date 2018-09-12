package org.srethink.ast

import org.srethink._
import org.scalatest._
import org.srethink.net.RethinkSpec

trait AstSpec extends RethinkSpec with BeforeAndAfterAll {

  val databaseName = "test_db"
  val tableName = "book"

  lazy val books = r.db(databaseName).table(tableName)

  override def beforeAll(): Unit = {
    create()
  }

  override def afterAll(): Unit = {
    drop()
  }

  private def create() = {
    rdb.run(r.dbCreate(databaseName)).attempt.unsafeRunSync()
    rdb.run(r.db(databaseName).tableCreate("book")).attempt.unsafeRunSync()
  }

  private def drop() = {
    ready(rdb.run(r.dbDrop("test_db")))
    ready(rdb.run(r.db(databaseName).tableDrop("book")))
  }
}
