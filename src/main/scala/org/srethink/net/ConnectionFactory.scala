package org.srethink.exec

import cats.syntax.all._
import cats.instances.vector._
import cats.effect._
import cats.effect.concurrent._
import org.srethink.net._

trait ConnectionFactory[F[_]] {
  val config: NettyConnectionConfig
  def get(): F[NettyConnection[F]]
}

class DefaultConnectionFactory[F[_]](
  val config: NettyConnectionConfig,
  val holders: Ref[F, Vector[NettyConnection[F]]],
)(implicit F: ConcurrentEffect[F], T: Timer[F]) extends ConnectionFactory[F] {

  def get(): F[NettyConnection[F]] = {
    holders.get.map { h =>
      h(scala.util.Random.nextInt(h.size))
    }
  }

  private def heal(c: NettyConnection[F]) = {
    c.closed().flatMap {
      case true =>
        holders.update(hs => (hs.filter(_ != c))) *> NettyConnection.create(config).flatTap { c =>
          holders.update(hs => hs :+ c)
        }
      case false =>
        F.pure(c)
    }
  }

}

object ConnectionFactory {
  def default[F[_]: ConcurrentEffect: Timer](size: Int, config: NettyConnectionConfig): F[ConnectionFactory[F]] = {
    val cons = Vector.fill(size)(NettyConnection.create(config)).sequence
    for {
      hs <- cons
      ref <- Ref[F].of(hs)
    } yield new DefaultConnectionFactory[F](config, ref)
  }
}
