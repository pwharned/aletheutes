package database

trait Expression[+T] extends Queryable[T] {
  def alias[T](aliasedName: String)(implicit column: Column[T]): Column[T] => Column[T] = ???
}

trait Aggregation[+T] extends Expression[T] {



  def agg[T](aliasedName: String, aggregation: String, args: String = "" )(implicit column: Column[T]):Column[T] => Column[T] = {
    val columnName = column.expression
    if (args==""){
      implicit column : Column[T] =>
        Column( columnName, aliasedName, aggregation = aggregation)
    }else {
      implicit column : Column[T] =>
        Column( columnName, aliasedName, aggregation = aggregation, args: String )
    }

  }
  def aggOver[T](aliasedName: String, aggregation: String, columns: Column[T]* )(implicit column: Column[T]):Column[T] => Column[T] = {
    val columnName = column.expression
    implicit column : Column[T] =>
      Column( columnName, aliasedName, aggregation = aggregation, over = columns:_*)
  }

}


trait Cast[+T] extends Expression[T] {

  def castTo[T](dtype: String )(implicit column: Column[T]):Column[T] => Column[T] = {
    val columnName = column.expression
    implicit column : Column[T] =>
      Column.apply( column, datatype = dtype)
  }
}


trait Count[+T] extends Aggregation[T] {

  override def alias[T](aliasedName: String)(implicit column: Column[T]) = {
    val columnName = column.columnName
    implicit column : Column[T] =>
      Column( columnName, aliasedName )

  }

  def countAs[T](aliasedName: String)(implicit column: Column[T]):Column[T] => Column[T] = agg(aliasedName, "COUNT")

}

trait Alias[+T] extends Expression[T] {

  override def alias[T](aliasedName: String)(implicit column: Column[T]) = {
    val columnName = column.columnName
    implicit column : Column[T] =>
      Column( columnName, aliasedName )
  }

}
