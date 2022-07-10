import java.sql.ResultSet
import database.{ConcreteDatabaseConfiguration, DB2, DatabaseConnection, GenericTable, Mapable}
import evaluators.ImpactEvaluator

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

object ImpactEvaluatorTest extends App {



    val currentDirectory = new java.io.File(".").getCanonicalPath

    println(currentDirectory +   "/project/database.json")


    val configuraiton = ConcreteDatabaseConfiguration( currentDirectory +   "/project/database.json")

    implicit val connection = DatabaseConnection(configuraiton)


  //case class Result(prediction: String, sex: String, group:String, disparate_impact: String, minutes: String, hours: String, days: String)
  case class Result(prediction: String, sex: String, group:Float,disparate_impact: Double, time:String)


    val resultTable = new ImpactEvaluator.Impact[Result, DB2]("risk", "test_data2", "sex", scoring_timestamp = "timestamp")





}
