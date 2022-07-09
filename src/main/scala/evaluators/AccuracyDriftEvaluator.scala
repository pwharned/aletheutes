package evaluators

import java.sql.ResultSet

import database.{AbstractDatabaseConnection, Column, GenericTable, Mapable, Model, Query, Table}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.runtime.universe._

case class AccuracyDriftResult( args: Map[String, Any])



object AccuracyDriftEvaluator {


  class Drift[T: TypeTag, A](table_name: String, features: Seq[String], scoring_timestamp: String = "scoring_timestamp", learn_rate: String,max_iter: String = "10000", target: String, implicit val connection: AbstractDatabaseConnection[A]) {
    val table = new GenericTable[T, A](name = table_name, values = features.map(x => new Column[Double](x)).toList )

    val model = table.asModel(learn_rate = learn_rate, max_iter = max_iter, target = target)


    val transactions = new GenericTable[T, A](name = table_name, values = features.map(x => new Column[Double](x)).toList:+ new Column[String](scoring_timestamp) ){

      def timestamp: Column[String] = new Column[String](scoring_timestamp)


      override def * : Query[Column[Any]] = super.*

    }



    def hourly = new GroupedTable{
      override def * = super.*.groupBy(timestamp.aggregate("hours", "hour"))
    }

    def dailyHourly = new GroupedTable{
      override def * = super.*.groupBy(timestamp.aggregate("days", "day"), timestamp.aggregate("hours", "hour"))
      println(*.columns)
    }
    def daily = new GroupedTable{
      override def * = super.*.groupBy(timestamp.aggregate("days", "day"))

    }

    class GroupedTable extends Table[T, A](name= "t1") {
      def timestamp: Column[String] = new Column[String](scoring_timestamp)
      def hour: Column[String] = timestamp.aggregate("hours", "hour")
      def day: Column[String] = timestamp.aggregate("days", "day")
      def equation = new Model( features.map(x => new Column[Double](x)).toList ){
        override def toString: String = f"SELECT AVG(${equation}*${equation}) as MSE,${timestamp.aggregate("time", "max")} FROM ${tableAlias}"
        override def equation: String = f"((${coefficients("B").zip(features).toMap.map( x=> f"(t1.${x._2}*t1.${x._1})" ).mkString("+")})+t1.INTERCEPT)"

        override def columns: List[Column[Any]] = List(new Column[String]("time"), new Column[Double]("mse"))


      }


      def * :Query[Column[Any]] =  equation
    }

    def compose(over: String) = over match {
      case "hourly" => (model + transactions)+ dailyHourly
      case "daily" => (model + transactions)+ daily
    }



    def retrieve(over: String):Future[ListBuffer[T]] =  {println(compose(over)); compose(over).flatMapToMap}

  }


def apply[A](table_name: String, features: Seq[String], scoring_timestamp: String = "scoring_timestamp", learn_rate: String,max_iter: String = "10000", target: String, over:String = "hourly", connection: AbstractDatabaseConnection[A]) =     new Drift[DriftResult, A](table_name, features, scoring_timestamp, learn_rate, max_iter, target, connection)


  def main(table_name: String, features: Seq[String], scoring_timestamp: String = "scoring_timestamp", learn_rate: String,max_iter: String = "10000", target: String, over:String = "hourly", connection: AbstractDatabaseConnection[_]): Future[ListBuffer[DriftResult]] =  {
    apply(table_name, features, scoring_timestamp, learn_rate, max_iter, target, over = over, connection= connection).retrieve(over=over)

  }


}