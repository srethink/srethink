package srethink.ast

import srethink.protocol.Response.ResponseType
import scala.concurrent._

class TableCreateDropSpec extends WithTestDatabase  {
  def table(name: String) = {
    DatumTerm(RStr(name))
  }

  "table create/drop ast"  should {
    "create and drop table" in {
      println("creating table bar")
      expectSuccessAtom(TableCreate(table("bar"), Some(database)))
      println("dropping table bar")
      expectSuccessAtom(TableDrop(table("bar"), Some(database)))
    }
  }
}
