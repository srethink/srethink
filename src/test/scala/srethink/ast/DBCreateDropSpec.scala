package srethink.ast

import org.specs2.mutable.Specification
import srethink.protocol.Response.ResponseType._

class DBCreateDropSpec extends Specification {

  def db(dbName: String) = {
   DatumTerm(RStr(dbName))
  }

  "db create ast" should {
    "create and drop db" in new WithTermQuery {
      val createFut = query(DBCreate(db("foo")).toTerm)
      createFut must beSome(SUCCESS_ATOM).await
      val dropFut = query(DBDrop(db("foo")).toTerm)
      dropFut must beSome(SUCCESS_ATOM).await
    }
  }
}
