package srethink.api

import srethink._

class TableCreateDropSpec extends WithTestDatabase  {

  "table create/drop ast"  should {
    "create and drop table" in {
      expectSuccessAtom(TableCreate(database, "bar"))
      expectSuccessAtom(TableDrop(database, "bar"))
    }
  }
}
