package org.srethink.ast

import io.circe._
import org.srethink.net._

trait Global {
  def epoch(time: Long) = Helper.term(TermType.EPOCH_TIME, Seq(Json.fromBigDecimal(BigDecimal(time) / 1000)))
}
