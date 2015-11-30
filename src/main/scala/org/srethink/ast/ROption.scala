package org.srethink.ast

import io.circe._

abstract class ROption[T:Encoder](key: String, value: T) {
  def valueJson = implicitly[Encoder[T]].apply(value)
}

case class RBooleanOption(_key: String, _value: Boolean) extends ROption(_key, _value)
case class RStringOption(_key: String, _value: String) extends ROption(_key, _value)
