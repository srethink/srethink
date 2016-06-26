package org.srethink

import org.srethink.ast._
import org.srethink.exec._
import org.srethink.net._

object r extends Global {

  type Config = NettyConnectionConfig

  def db(name: String) = new DB(name)

  def dbCreate(name: String) = new DBCreate(name)

  def dbDrop(name: String) = new DBDrop(name)

  def executor(cfg: Config = new NettyConnectionConfig,
    dateTimeFormat: String = "yyyy-MM-dd HH:mm:ss",
    timezone: String = "+00:00") = new QueryExec(
    ExecConfig(new AutoReconnectConnectionFactory(cfg), dateTimeFormat, timezone)
  )
}
