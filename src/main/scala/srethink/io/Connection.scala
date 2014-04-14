package srethink.io

trait Connection {
  def connect(config: HostConfig)
}
