package database
import scala.reflect.runtime.universe._


trait Executable

class Query[+T<:Column[Any]] (values: List[T] )(implicit tableName: String) extends Executable {

  implicit def queryToModel(target: String, learn_rate: String, max_iter: String)(implicit query: Query[Column[Any]]= this): Model = new Model(values=values, learn_rate = learn_rate, target=target, max_iter=max_iter)


  def drop[A<:Column[_]](column: A*): Query[T] = new Query(values.filter( x=> !column.map(c=>c.columnName).contains(x.columnName)))


  def columns: List[T] = values

  def cl[T]: String = columns.map( x=>x match {
    case x: Column[T] => x.toString
  }).mkString(",")

  override def toString: String = f"SELECT $cl FROM $tableName"

  def reSource(sourceName: String) = toString.replace(tableName, sourceName)

  def asCte(cteName: String): String = f"$cteName as (" + reSource(tableName) + ")"


  def select[A<:Column[_]](column: A*): Query[T] = new Query(values.filter(x => column.map(c => c.columnName).contains(x.columnName )))


  def groupBy[A<: Any](groupByColumns: Column[A]*): Query[T] = {
    val query: String = this.toString
    new Query(columns)
    {
      override def toString = query + (this :: Mutatable.GroupAble).clause(groupByColumns.map(x =>x.expression):_* ).apply(groupByColumns.map(x => x.expression).mkString(","))

    }
  }

  def filter[A<: Any](filterColumn: Column[A], operator: String, value: String ): Query[T] = {
    val condition =  Condition(filterColumn, operator, value)
    val query: String = this.toString
    new Query(values)
    {
      override def toString = query + (this :: Mutatable.Filterable).clause(condition ).apply(filterColumn.expression)

    }
  }

  def filter[A<: Any, B: TypeTag](filterColumn: Column[A], operator: String, table: Table[B, A] ): Query[T] = {
    val condition =  Condition(filterColumn, operator, table)
    val query: String = this.toString
    new Query(values)
    {
      override def toString = query + (this :: Mutatable.Filterable).clause(condition ).apply(filterColumn.expression)

    }
  }
}


class EmptyQuery[T<:Column[Nothing]] (implicit tableName: String) extends Query[T](List()) {

  val values: List[T]  = List()

  override def drop[A<:Column[_]](column: A*): Query[Nothing] = new EmptyQuery()


  override def columns: List[T] = values

  override def cl[T]: String = ""

  override def toString: String = f"SELECT $cl FROM $tableName"

  override def asCte(tableName: String): String = f"$tableName as (" + toString + ")"


  override def select[A<:Column[_]](column: A*): Query[Nothing] = new EmptyQuery()


  override def groupBy[A<: Any](groupByColumns: Column[A]*):Query[Nothing] = new EmptyQuery()

  override def filter[A<: Any](filterColumn: Column[A], operator: String, value: String ): Query[Nothing] = new EmptyQuery()

  override def filter[A<: Any, B: TypeTag](filterColumn: Column[A], operator: String, table: Table[B, A] ): Query[Nothing] = new EmptyQuery()
}


object Query{
  ///def apply[T <: Any](values: List[Column[T]])(implicit tableName: String): Query[T] = new Query(values)

  def apply(values: List[Column[Any]])(implicit tableName: String): Query[Column[Any]] = new Query(values)

}

