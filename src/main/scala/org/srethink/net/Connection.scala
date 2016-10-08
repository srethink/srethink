package org.srethink.net

import scala.concurrent.Future

trait Connection {

  def execute(message: Message): Future[Message]

  def close(): Unit

  def connect(): Unit

  def closed(): Future[Boolean]
}
