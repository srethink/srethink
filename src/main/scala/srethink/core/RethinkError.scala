package srethink.core

class ConnectionError(val message: String) extends Exception(message)

class QueryError(val message: String, val backtrace: String)
    extends Exception(s"message: ${message}, backtrace: ${backtrace}")
