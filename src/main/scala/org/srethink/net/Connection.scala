package org.srethink.net


trait Connection[F[_]] {

  def execute(message: Message): F[Message]

  def close(): F[Unit]

  def closed(): F[Boolean]
}
