package srethink.api

import srethink.ast._
import srethink.net._
import AstHelper._

trait R { this: QueryDSL =>
  val connection: Connection
  def table(name: String) = new Table(new RTable(name), connection)

  class Table(
    table: RTable,
    conn: Connection
  ) extends Sequence(table, conn) {
    def get[T: RDecoder](pk: RDatum) = {
      val decoder = implicitly[RDecoder[T]]
      singleSelect[T](Get(table, new DatumTerm(pk)), decoder)
    }
  }

  class Sequence(
    val seq: RTerm,
    val connection: Connection) extends TermQuery {

    def filter(func: RDynamic => RPredicate) = {
      val body = function1 { arg =>
        func(RDynamic(arg))
      }
      new Sequence(Filter(seq, body), connection)
    }

    def map(func: RDynamic => RTerm) {
      val body = function1 { arg =>
        func(RDynamic(arg))
      }
      new Sequence(Map(seq, body), connection)
    }

    def as[T: RDecoder] = {
      val decoder = implicitly[RDecoder[T]]
      sequenceSelect(seq, decoder)
    }
  }

}
