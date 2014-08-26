package play.api

import srethink.ast._
import srethink.net._

package object rql extends RQL with PlayJsonDef {

  implicit lazy val queryExecutor = {
    new NettyQueryExecutor(PlayRethinkConfig.rethinkConfig)
  }
}
