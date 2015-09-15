package srethink

case class Book(
  id: Option[String] = None,
  title: String,
  author: String,
  coAuthors: Seq[CoAuthor] = Seq(),
  price: Double,
  quantity: Int,
  releaseDate: java.util.Date)

case class CoAuthor(
  name: String,
  alias: Seq[String])
