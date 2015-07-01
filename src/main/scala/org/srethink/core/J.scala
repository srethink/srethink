package org.srethink.core

import scala.util.Try

trait J {
  type JSON
  def stringify(j: JSON): String
  def parse(j: String): Try[JSON]
}
