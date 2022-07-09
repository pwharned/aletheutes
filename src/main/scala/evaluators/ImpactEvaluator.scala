package evaluators

import database._

import scala.reflect.runtime.universe._

object ImpactEvaluator {

  class Impact[T: TypeTag, A](prediction: String, table: String, protected_column: String, scoring_timestamp: String  = "scoring_timestamp") (implicit connection: AbstractDatabaseConnection[A]){


    class TimeTable extends Table[T, A](name= table) {
      def prediction_column = Column[String](prediction).as("prediction")
      def protected_attribute_column = Column[String](name = protected_column)
      def minutes = Column[String](name = scoring_timestamp).aggregate("minutes", "minute")
      def hours = Column[String](name = scoring_timestamp).aggregate("hours", "hour")
      def maxDays = Column[String](name = scoring_timestamp).aggregate("days", "day")
      def days = Column[String](name = scoring_timestamp).aggregate("days", "day")

      //def time = Column[String](name = scoring_timestamp).aggregateOver("time", "max", hours)
      def time = Column[String](name = scoring_timestamp).aggregateOver("time", "max", hours, days)
      //def * =  (prediction_column, time, protected_attribute_column).filter(days, "=", table = maxDaysTable)

      def * =  (prediction_column, time, protected_attribute_column)
    }

    class GroupedTable extends Table[T, A](name= "t1") {
      def prediction_column = Column[String]("prediction")
      def protected_attribute_column = Column[String](name = protected_column).count("group")
      def time = Column[String](name = "time")

      def * =  (prediction_column,protected_attribute_column, protected_attribute_column.unpack, time) .groupBy(protected_attribute_column.unpack, prediction_column, time)
    }

    class MaxDaysTable extends Table[T, A](name = table) {
      def maxDays = Column[String](name = scoring_timestamp).aggregate("days", "max").aggregate("days", "day")

      def * = maxDays

    }

    class RatiosTable extends Table[T, A](name = "t2") {
      def prediction_column = Column[String](name= "prediction")
      def protected_attribute_column = Column[String](name = protected_column)
      def group = Column[Float](name = "group").cast(datatype = "float")
      def time = Column[String](name = "time")

      def ratio = Column[Double](name = "group").aggregateOver("ratios", "sum", protected_attribute_column,time)

      def ratios = group/ratio

      def * = (prediction_column, protected_attribute_column,group.as("group"),time, ratios)

    }

    class ResultsTable extends RatiosTable {

      override  val tableName: String = "t3"

      override def ratio = Column[Double](name = "ratios")
      def ratioAsFloat =Column[Double](name = "ratios").cast("float")
      def differences = Column[Double](name = "ratios").aggregateOver("disparate_impact", "sum", prediction_column, time)-ratio
      def disparate_impact = differences.caseWhen("=", "0", "1", ratioAsFloat/differences)

      override def * = (prediction_column, protected_attribute_column, group.as("group"), disparate_impact, time)

    }

    def maxDaysTable = new MaxDaysTable()

    def result = new TimeTable() + new GroupedTable() + new RatiosTable() + new ResultsTable()


  }

}


