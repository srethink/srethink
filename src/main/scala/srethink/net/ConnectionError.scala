package srethink.net

class ConnectionError(val message: String) extends Exception(message)
class RethinkException(val message: String) extends Exception(message)
