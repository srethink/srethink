package srethink.dsl

import srethink.ast._
import srethink.api._

class TableDSL(table: RTable)
    extends SequenceDSL(table) {

  def get(id: DatumTerm[_ <: RDatum]) = new Atom(Get(table, id))

  def insert[T: REncoder](items: T*) = {
    val encoder = implicitly[REncoder[T]]
    new Atom(Insert(table, items.map(encoder.encode)))
  }
}
