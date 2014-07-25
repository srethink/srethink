package srethink.dsl

import srethink.api._

case class IdNamePair(
  id: Long,
  name: String
)
object IdNamePair {
  implicit val decoder: RDecoder[IdNamePair] = CodecMacros.decoder[IdNamePair]
}
class WithFieldsSpec extends DSLSpec {
  "with fields" should {
    "get fields of objects" in {
      val matchers = for {
        succ <- persons.insert(man).run if succ
        r <- persons.fields("id", "name").first[IdNamePair]
      } yield {
        r must be_==(IdNamePair(man.id.get, man.name))
      }
      matchers.await
    }
  }
}
