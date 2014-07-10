package srethink.dsl

import srethink._
import srethink.api._
import java.util.Date

case class Person(
  id: Option[Long],
  name: String,
  birth: Date,
  height: Int,
  pair: Option[Person],
  children: Seq[Person])



object Person {
  implicit val encoder = CodecMacros.encoder[Person]
  implicit val decoder = CodecMacros.decoder[Person]
}

trait DSLSpec extends WithTestTable {

  implicit lazy val queryExecutor = new QueryExecutor {
    val connection = DSLSpec.this.connection
  }

  val boy = Person(Some(2), "boy",  yearsAgo(10), 130, None, Nil)
  val women = Person(Some(3), "woman", yearsAgo(27), 160, None, Nil)
  val man = Person(Some(1), "man",  yearsAgo(30), 175, Some(women), boy :: Nil)

  private def yearsAgo(years: Int) = {
    val now = System.currentTimeMillis
    val t = now - (365L * 24L * 3600L * 1000L) * years
    new Date(t)
  }
}
