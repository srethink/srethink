package srethink.api

import  AstHelper._
class DSL(
  val term: RTerm) {

  def filter(func: RDynamic => RPredicate) = {
    val body = function1 { arg =>
      func(RDynamic(arg))
    }
    new DSL(Filter(term, body))
  }

  def map(func: RDynamic => RTerm) = {
    val body = function1 { arg =>
      func(RDynamic(arg))
    }
    new DSL(Map(term, body))
  }

  def limit(size: Int) = {
    new DSL(Limit(term, size))
  }

  def skip(offset: Int) = {
    new DSL(Skip(term, offset))
  }

  def fields(fields: String*) = {
    new DSL(WithFields(term, fields.map(strAsTerm)))
  }

  def group(field: String) = {
    new DSL(Group(term, field))
  }

  def ungroup() = {
    new DSL(UnGroup(term))
  }

  def sum(field: String) = {
    new DSL(SUM(term, field))
  }

  def count() = {
    new DSL(Count(term))
  }

  def get(id: DatumTerm[_ <: RDatum]) = new DSL(Get(term.asInstanceOf[RTable], id))

  def insert[T: REncoder](items: T*) = {
    val encoder = implicitly[REncoder[T]]
    new DSL(Insert(term.asInstanceOf[RTable], items.map(encoder.encode)))
  }
}
