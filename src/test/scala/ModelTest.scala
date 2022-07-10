import ImpactEvaluatorTest.configuraiton
import database.{Column, ConcreteDatabaseConfiguration, DB2, DatabaseConnection, GenericTable, Model, Query}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object ModelTest extends App {

    /*
    Test that a table can be created from a sequence of columns
     */

  val currentDirectory = new java.io.File(".").getCanonicalPath

  print(currentDirectory +   "/project/database.json")

  val configuraiton = ConcreteDatabaseConfiguration( currentDirectory +   "/project/database.json")

    val columnA: Column[Double] = Column("checkingstatus")
    val columnB: Column[Double] = Column("loanduration")
  val columnc: Column[Double] = Column("job")

  val columnSeq: List[Column[_]] = List(columnA, columnB, columnc)
    case class Result( b2: Double*)
  implicit val connection = DatabaseConnection(configuraiton)

  val table = new GenericTable[Result, DB2](name = "scored_credit", values = columnSeq)

  val model = table.*.queryToModel(learn_rate = ".012", max_iter = "2000", target = "prediction")

  val modelTable = table.asModel("prediction", ".001", "10000")

  println(modelTable)
  println(model.equation)


  //print(Result(myargs:_*))

  println(modelTable.*.columns)
  //println(modelTable.*.columns.map(x=> x.alias))

 ///println(Await.result(modelTable.flatMap, 20.seconds))


    /*
    Test that a query can be built from a sequence of columns
     */
    //val query = new Query(columnSeq).asInstanceOf[Query[Column[Any]]]




}
