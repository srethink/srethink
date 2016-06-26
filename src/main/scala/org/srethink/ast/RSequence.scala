package org.srethink.ast

import io.circe._

case class RSequence(term: Json) {
}

case class RTable(name: String, options: Seq[ROption[_]]) {
}

case class RSelectArray extends RSequence()
