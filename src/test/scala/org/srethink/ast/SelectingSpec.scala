package org.srethink.ast

import io.circe.generic.auto._

trait SelectingSpec extends AstSpec with SelectingData {
  override def beforeAll(): Unit = {
    super.beforeAll()
    ready(rdb.run(books.insert(book1, book2, book3)))
  }
}



trait SelectingData {

  case class Book(
    id: Long,
    price: Long,
    author: String
  )


  val book1 = Book(1L, 1L, "foo")
  val book2 = Book(2L, 2L, "bar")
  val book3 = Book(3L, 3L, "baz")


}
