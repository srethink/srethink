package srethink.dsl

import srethink.ast._
import srethink.api._
import  AstHelper._
class SequenceDSL(
  val seq: RTerm) extends DSL {

  def filter(func: RDynamic => RPredicate) = {
    val body = function1 { arg =>
      func(RDynamic(arg))
    }
    new SequenceDSL(Filter(seq, body))
  }

  def map(func: RDynamic => RTerm) = {
    val body = function1 { arg =>
      func(RDynamic(arg))
    }
    new SequenceDSL(Map(seq, body))
  }

  def limit(offset: Int, size: Int) = {
    new SequenceDSL(Limit(Skip(seq, offset), size))
  }

  def toTerm = seq
}
