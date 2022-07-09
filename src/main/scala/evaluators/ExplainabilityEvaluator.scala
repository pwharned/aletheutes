package evaluators

import java.sql.ResultSet

import database.{AbstractDatabaseConnection, Column, GenericTable, Query}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.reflect.runtime.universe._

case class ExplanationResult( args: Map[String, Any])



object ExplainabilityEvaluator {



  class Explanation[T: TypeTag, A](table_name: String, target: String, features: Seq[String],id_column: String,  scoring_timestamp: String = "scoring_timestamp",  learn_rate: String, max_iter: String, ids: Seq[Int],implicit val connection: AbstractDatabaseConnection[A]) {

    val table = new GenericTable[T, A](name = table_name, values = features.map(x => new Column[Double](x)).toList )

    val transactions = new GenericTable[T, A](name = table_name, values = features.map(x => new Column[Double](x)).toList:+new Column[Double](id_column) ){
      def scoring_id: Column[Double] = new Column[Double](id_column)
      override def * : Query[Column[Any]] = super.*.filter(scoring_id,"in", f"(${ids.mkString(",")})"  )
    }

    val model = table.asModel(learn_rate = learn_rate, max_iter = max_iter, target = target)


    def compose =  model + transactions

    def retrieve:Future[ListBuffer[T]] =  {println(compose);compose.flatMap}




  }

  def main[A](table_name: String, target: String, features: Seq[String],id_column: String, scoring_timestamp: String = "scoring_timestamp", learn_rate: String, max_iter: String, ids: Seq[Int], connection: AbstractDatabaseConnection[A]): Future[ListBuffer[ExplanationResult]] =  {


    new Explanation[ExplanationResult,A](table_name, target, features, id_column, scoring_timestamp=scoring_timestamp, learn_rate, max_iter,ids = ids, connection=connection).retrieve

  }



}