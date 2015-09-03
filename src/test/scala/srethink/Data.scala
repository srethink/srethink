package srethink

import play.api.libs.json._

case class Book(
  id: Option[String] = None,
  title: String,
  author: String,
  coAuthors: Seq[String] = Seq(),
  price: Double,
  quantity: Int,
  desc: JsString,
  releaseDate: java.util.Date)
