package srethink.dsl

import srethink.ast._
import srethink.api._

class TableDSL(table: RTable)
    extends SequenceDSL(table) {

  def get(id: DatumTerm[_ <: RDatum]) = Get(table, id)

  def insert[T: REncoder](t: T) = {
    val encoder = implicitly[REncoder[T]]
    Insert(table, DatumTerm(encoder.encode(t)))
  }
}
