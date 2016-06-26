package org.srethink

import io.circe._

abstract class TableQuery[A: Encoder, K: Encoder] {
  def get(k: K)
}
