package org.srethink.net

import scala.concurrent.Future
import org.srethink.core._

trait Connection {
  def execute(message: Message): Future[Message]
}
