package org.srethink

import io.circe._

trait AstShow[A] {
  def show[A]: Json
}

trait AstShows {
}
