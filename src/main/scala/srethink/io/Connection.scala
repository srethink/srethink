package srethink.io

import akka.actor.{ Actor, ActorRef, Props }
import akka.io._
import akka.util.ByteString
import java.net.InetSocketAddress
import srethink.core._

class Connection(config: HostConfig) extends Actor with ActorLogging {

  def connect() = {
    import Tcp._
    import context.system
    val address = InetSocketAddress(config.hostname, config.port)
    IO(Tcp) ! Connect(address)
  }

  def receive = {
    case CommandFailed(_: Connect) =>
      log.info(s"connect to ${config} failed, shutdown actor!")
      context stop self
    case Connected(remote, local) =>
      log.info(s"connect to ${config} success, begin event loop")
      context.become
  }

}
