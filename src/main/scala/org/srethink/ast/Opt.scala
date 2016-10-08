package org.srethink.ast

import io.circe._

case class Opt(key: String, value: Json) {
  def pair = key -> value
}
