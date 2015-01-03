package srethink

case class Book(
  id: Option[String] = None,
  title: String,
  author: String,
  coAuthors: Seq[String] = Seq(),
  price: Double,
  quantity: Int,
  releaseDate: java.util.Date)
