package play.api.rql

import play.api.Play.current
import srethink.net._

object PlayRethinkConfig {

  lazy val rethinkConfig = {
    val cfg = current.configuration
    val host = cfg.getString("rethinkdb.host").getOrElse("localhost")
    val port = cfg.getInt("rethinkdb.port").getOrElse(28015)
    val authKey = cfg.getString("rethinkdb.auth").getOrElse("")
    RethinkConfig.nettyConfig(host, port, authKey)
  }
}
