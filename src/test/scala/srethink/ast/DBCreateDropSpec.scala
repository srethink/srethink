package srethink.ast

import org.specs2.specification.Scope
import srethink.protocol.Response.ResponseType._
import srethink._

class DBCreateDropSpec extends TermSpec {
  "db create ast" should {
    "create and drop db" in  {
      expectSuccessAtom(DBCreate(db("foo")))
      expectSuccessAtom(DBDrop(db("foo")))
     }
  }
}
