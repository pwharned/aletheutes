import ModelTest.configuraiton
import TableTest.columnA.columnName
import database.{Column, ConcreteDatabaseConfiguration, DB2, DatabaseConnection, GenericTable, Query, Table}
object  TableTest extends App {

  /*
  Test that a table can be created from a sequence of columns
   */
  implicit val connection = DatabaseConnection(configuraiton)
  val currentDirectory = new java.io.File(".").getCanonicalPath

  print(currentDirectory +   "/project/database.json")

  val configuraiton = ConcreteDatabaseConfiguration( currentDirectory +   "/project/database.json")

  val columnA: Column[String] = Column("columnA")
  val columnB: Column[Int] = Column("columnB")
  val columnSeq: List[Column[_]] = List(columnA, columnB)
  case class Result(columnA: String, columnB: String)
  val table = new GenericTable[Result, DB2](name = "testTable", values = columnSeq)

  println(table)

  /*
  Test that a query can be built from a sequence of columns
   */
  val query = new Query(columnSeq).asInstanceOf[Query[Column[Any]]]

  println(query)


}
