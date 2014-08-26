package srethink

import play.api.rql._

case class Book(
  title: String,
  author: String,
  price: Double,
  quantity: Int,
  releaseDate: java.util.Date)
