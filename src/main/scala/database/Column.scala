package database
import java.sql.ResultSet

import scala.language.implicitConversions
import scala.reflect.runtime.universe._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future




class Column[+T<:Any](name: String)(implicit retriever: Queryable[T]) extends Aggregation[T] with Alias[T] with Count[T] with Cast[T]  {
  override def toString: String = name

  def alias: String = name
  def expression: String = name

  implicit val column: Column[T] = this
  implicit val columnName: String = name
  def as[A>:T](aliasedName: String =name)(implicit column: Column[A] = this) = super.alias(aliasedName).apply(this)
  def count[A>:T](aliasedName: String =name)(implicit column: Column[A] = this) = super.countAs(aliasedName).apply(this)
  def aggregate[A>:T](aliasedName: String =name, aggregation: String, args:String="")(implicit column: Column[A] = this) = super.agg(aliasedName,aggregation, args = args ).apply(this)

  def aggregateOver[A>:T](aliasedName: String, aggregation: String, overColumns: Column[A]*)(implicit column: Column[A] = this) = super.aggOver(aliasedName,aggregation, overColumns:_* ).apply(this)

  def unpack: Column[T] = Column(columnName)

  def /[A>:T](div: Column[A]): Column[T] ={
    val numeratorExpression = this.expression
    val divisorExpression = div.expression
    val leftAlias = div.alias

    new Column[T](name=column.columnName){
      override def toString = expression + " as " + leftAlias
      override def expression: String = "(" +  numeratorExpression + "/" + divisorExpression + ")"
      override def alias: String = leftAlias
    }
  }

  def caseWhen[A>:T](operator: String, condition: String, value: String, expression: Column[A]): Column[T] = {
    val leftExpression: String = f"case when   ${this.expression} $operator $condition then $value else ${expression.expression} end as ${this.alias}"
    val leftAlias = this.alias

    new Column[T](name = this.columnName){
      override def toString: String = leftExpression
      override def alias: String = leftAlias
    }

  }

  def -[A>:T](operand: Column[A]): Column[T] ={
    val leftAlias = this.alias
    val leftExpression = this.expression
    val rightExpression = operand.expression

    new Column[T](name=column.columnName){
      override def expression: String = "(" +  leftExpression + "-" + rightExpression + ")"
      override def toString = expression + " as " + leftAlias
      override def alias: String = leftAlias

    }
  }

  def *[A>:T](div: Column[A]): Column[T] = {
    val divisor = div.toString
    val numerator = this.toString
    new Column[T](name = column.columnName) {
      override def toString = numerator + "*" + divisor
    }
  }

  def +[A>:T](div: Column[A]): Column[T] = {
    val divisor = div.toString
    val numerator = this.toString
    new Column[T](name = column.columnName) {
      override def toString = numerator + "+" + divisor
    }
  }
  def cast[A>:T](datatype: String )(implicit column: Column[A] = this) = super.castTo( dtype = datatype).apply(this)


  def retrieve(columnName: String,resultSet: ResultSet): Any = retriever.retrieve(columnName, resultSet)
  def retrieve(columnName: String, resultSet: Future[ResultSet]): Future[Any] = resultSet.map(result => retriever.retrieve(columnName, resultSet))


}

object Column {

  def apply[T](name: String)(implicit retriever: Queryable[T]): Column[T] = new Column[T](name=name)

  def apply[T](name: String, columnAlias: String)(implicit retriever: Queryable[T]): Column[T] = new Column[T](name=name){
    override def toString: String = f"$name as $columnAlias"
    override def alias: String = columnAlias
  }


  def apply[T](name: String, columnAlias: String, aggregation: String)(implicit retriever: Queryable[T]): Column[T] = new Column[T](name=name){
    override def toString: String = f"$aggregation($name) as $columnAlias"
    override def alias: String = columnAlias
    override def expression: String = f"$aggregation($name)"
  }

  def apply[T](name: String, columnAlias: String, aggregation: String, args: String )(implicit retriever: Queryable[T]): Column[T] = new Column[T](name=name){
    override def toString: String = f"$aggregation($args, $name) as $columnAlias"
    override def alias: String = columnAlias
    override def expression: String = f"$aggregation($args, $name)"
  }

  def apply[T](name: String, columnAlias: String, aggregation: String, over: Column[T]*)(implicit retriever: Queryable[T]): Column[T]= {
    val newExpression = f"$aggregation($name) OVER( PARTITION BY ${over.map(x => x.expression).mkString(",")} )"
    new Column[T](name=name){
      override def toString: String = f"$expression as $columnAlias"
      override def alias: String = columnAlias
      override def expression: String = newExpression
    }

  }
  def apply[T](column: Column[T], datatype: String )(implicit retriever: Queryable[T]): Column[T] = new Column[T](name=column.columnName){
    override def toString: String = f"cast(${column.columnName} as $datatype)"
    override def expression: String = f"cast(${column.columnName} as $datatype)"
  }

  def apply[T](column: Column[T])(implicit retriever: Queryable[T]): Column[T] = new Column[T](name=column.columnName)

}



///Map to shoudl return a function which implicitly maps to a collection of the case class