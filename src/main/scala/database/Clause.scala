package database

trait Clause

class Condition(column: Column[_], operator: String, value: String){
  override def toString: String = f"${column.expression} ${operator} ${value}"
}

object Condition {
  def apply(column: Column[_], operator: String, value: String): Condition = new Condition(column, operator, value)
  def apply[T, A](column: Column[_], operator: String, value: Table[T, A]): Condition = new Condition(column, operator, "(" +  value.*.toString + ")")

}

trait GroupBy extends Clause{

  implicit def clause(columnName: String*): String => String = {
    val columns = columnName.mkString(" , ")
    implicit transformer: String  => {

      s" GROUP BY $columns"
    }}
}

trait Filter extends Clause{

  implicit def clause(conditions: Condition*): String => String = {
    val columns = conditions.mkString(" and ")
    implicit transformer: String  => {

      s" WHERE $columns"
    }}
}


object Mutatable {
  implicit def innerObj[T](o: Mutated[T]):T = o.obj

  def ::[T](o: T) = new Mutated(o)

  class Mutated[+T] private[Mutatable](val obj: T) extends Clause

  object GroupAble {
    implicit def innerObj[T](o: Grouped[T]):T = o.obj

    def ::[T](o: T) = new Grouped(o)

    final class Grouped[+T] private[GroupAble](override val obj: T) extends Mutated[T](obj = obj) with GroupBy


  }
  object Filterable {
    implicit def innerObj[T](o: Filtered[T]):T = o.obj

    def ::[T](o: T) = new Filtered(o)

    final class Filtered[+T] private[Filterable](override val obj: T) extends Mutated[T](obj = obj) with Filter


  }

}




