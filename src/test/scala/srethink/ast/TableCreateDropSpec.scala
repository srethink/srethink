package srethink.ast

class TableCreateDropSpec extends WithTestDatabase  {

  "table create/drop ast"  should {
    "create and drop table" in {
      expectSuccessAtom(TableCreate(tb("bar")))
      expectSuccessAtom(TableDrop(tb("bar")))
    }
  }
}
