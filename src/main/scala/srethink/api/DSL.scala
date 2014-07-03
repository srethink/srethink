package srethink.api

import srethink.ast._
import srethink.net._
import AstHelper._

trait R { this: QueryDSL =>
  val connection: Connection
  def table(name: String) = new Table(RTable(DatumTerm(RStr(name))), connection)

  class Table(
    table: RTable,
    conn: Connection
  ) extends Sequence(table, conn) {

    def get[T: DatumDecoder](pk: RDatum) = {
      val decoder = implicitly[DatumDecoder[T]]
      singleSelect[T](Get(table, DatumTerm(pk)), decoder)
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

    def as[T: DatumDecoder] = {
      val decoder = implicitly[DatumDecoder[T]]
      sequenceSelect(seq, decoder)
    }
  }

}
