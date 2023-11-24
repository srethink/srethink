package org.srethink

import cats.syntax.all._
import cats.effect._
import org.srethink.ast._
import org.srethink.exec._
import org.srethink.net._

object r extends Global {

  type Config = NettyConnectionConfig

  type RDB[F[_]] = QueryExec[F]
  type DB = org.srethink.ast.DB



  def db(name: String) = new DB(name)

  def dbCreate(name: String) = new DBCreate(name)

  def dbDrop(name: String) = new DBDrop(name)

  def executor[F[_]: Async](cfg: Config = new NettyConnectionConfig,
    dateTimeFormat: String = "yyyy-MM-dd HH:mm:ss",
    timezone: String = "+00:00") = {
    ConnectionFactory.default[F](16, cfg).map { factory =>
      val execCfg = new ExecConfig(
        factory,
        dateTimeFormat,
        timezone
      )
      new QueryExec(execCfg)
    }
  }
}
