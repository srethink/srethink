package org.srethink.net

import scala.concurrent.Future

trait Connection {
  def execute(message: Message): Future[Message]

  def close(): Future[Unit]

  def connect(): Future[Unit]
}
