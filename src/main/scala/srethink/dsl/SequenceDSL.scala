package srethink.dsl

import srethink.ast._
import srethink.api._
import  AstHelper._
class SequenceDSL(
  val term: RTerm) extends DSL {

  def filter(func: RDynamic => RPredicate) = {
    val body = function1 { arg =>
      func(RDynamic(arg))
    }
    new SequenceDSL(Filter(term, body))
  }

  def map(func: RDynamic => RTerm) = {
    val body = function1 { arg =>
      func(RDynamic(arg))
    }
    new SequenceDSL(Map(term, body))
  }

  def limit(offset: Int, size: Int) = {
    new SequenceDSL(Limit(Skip(term, offset), size))
  }

  def fields(fields: String*) = {
    new WithFields(term, fields.map(strAsTerm))
  }

  def sum(field: String) = {
    new SUM(term, field)
  }
}