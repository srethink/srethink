package org.srethink.net

trait Func
trait Index[T]
trait Predicate

class Selection[T] {
  def map[U](f: T => U): Selection[U]
  def filter(f: T => Predicate): Selection[U]
}

class SingleSelection[T] {
}

class Table[T, PK: Datum] extends Selection[T] {
  def get(v: T, index: Index[T]): SingleSelection[T]
  def getAll(v: Seq[T], index: Index[T])
}

case class User(id: Long, name: String)

trait Field

trait Selection[T] {

  def toTerm
}
