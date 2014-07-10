package srethink.api

import srethink.ast._
import AstHelper._
import srethink.dsl._

trait TermImplicits {
  implicit class RDynamic(val term: RTerm) extends Dynamic {
    def selectDynamic(name: String) = {
      GetField(term, strTerm(name))
    }
  }

  implicit class RTermOps(val term: RTerm) {
    def === (that: RTerm) = EQ(term, that)
    def > (that: RTerm) = GT(term, that)
    def < (that: RTerm) = LT(term, that)

    def first[T: RDecoder](implicit executor: QueryExecutor) = {
      executor.headOption[T](term)
    }

    def list[T: RDecoder](implicit executor: QueryExecutor) = {
      executor.take[T](term)
    }

    def run(implicit executor: QueryExecutor) = {
      executor.run(term)
    }

  }

  @inline implicit def dslAsRTermOps(dsl: DSL) = RTermOps(dsl.toTerm)
  @inline implicit def strAsTerm(value: String)= strTerm(value)
  @inline implicit def intAsTerm(value: Int) = numTerm(value)
  @inline implicit def longAsTerm(value: Long) = numTerm(value)
  @inline implicit def floatAsTerm(value: Float) = numTerm(value)
  @inline implicit def doubleAsTerm(value: Double) = numTerm(value)
  @inline implicit def booleanAsTerm(value: Boolean) = boolTerm(value)
}
