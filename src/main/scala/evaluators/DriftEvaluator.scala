package evaluators

import java.sql.ResultSet

import database.{AbstractDatabaseConnection, Column, GenericTable, Mapable, Query, Table}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.runtime.universe._

case class DriftResult( args: Map[String, Any])



object DataDriftEvaluator {



  class Drift[T: TypeTag, A](table_name: String, features: Seq[String], scoring_timestamp: String = "scoring_timestamp", measure: String,over: String = "hours",  implicit val connection: AbstractDatabaseConnection[A]) {

    def hourly = new RatiosTable{
      override def * = super.*.groupBy(timestamp.aggregate("hours", "hour"))
    }

    def dailyHourly = new RatiosTable{
      override def * = super.*.groupBy(timestamp.aggregate("hours", "hour"), timestamp.aggregate("day", "day"))
    }
    def daily = new RatiosTable{
      override def * = super.*.groupBy(timestamp.aggregate("days", "day"))
    }

    class RatiosTable extends Table[T, A](name = table_name) {

      def timestamp = new Column[String](scoring_timestamp)

       def * = features.map(x => new Column[Double](x)  ).map(col => col.aggregate(col.alias, measure) ).toList :+timestamp.aggregate("time", "max")

      override def map[T](implicit converter: Mapable.CaseMapable[T]  = converter ) = { implicit execution: Execution =>
        execution.map{
          val l = ListBuffer.empty[T]
          result => while(result.next()){
            l+= converter.mapToMap(*.columns.map(column => column.alias ->  column.retrieve(column.alias,resultSet = result)).toMap )

          }
            l
        }
      }

    }

    def compose(over: String) = over match {
      case "hourly" => dailyHourly
      case "daily" => daily
    }

    def retrieve(over: String):Future[ListBuffer[T]] =  compose(over).flatMap
  }



  def main[A](table_name: String, features: Seq[String], scoring_timestamp: String = "scoring_timestamp", measure: String, over: String,  connection: AbstractDatabaseConnection[A]): Future[ListBuffer[DriftResult]] =  {

    new Drift[DriftResult, A](table_name = table_name, features=features, scoring_timestamp=scoring_timestamp, measure=measure, connection=connection).retrieve(over)


  }


}