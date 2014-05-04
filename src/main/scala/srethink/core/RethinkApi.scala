package srethink.core


trait Api[T]

case class DBCreate(dbName: String) extends Api[Boolean]

trait JsonTypes {
  type JsonArray
  type JsonObject
}

trait ApiConstants {

  object Durablity {
    val Hard = "hard"
    val Soft = "sort"
  }
}
