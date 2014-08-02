package srethink.api

case class NameGroup(group: String, reduction: Seq[Person])

class GroupSpec extends DSLSpec {
  implicit val decoder = CodecMacros.decoder[NameGroup]
  "group" should {
    "group sequence" in {
      val matchers = for {
        succ <- persons.insert(man).run  if succ
        g <- persons.group("name").ungroup().first[Seq[NameGroup]]
      } yield {
        g must have size(1)
      }
      matchers.await
    }
  }
}
