package database
import java.sql.ResultSet

object Result {

  implicit class ResultSetStream(resultSet: ResultSet) {

    def toStream: Stream[ResultSet] = {
      new Iterator[ResultSet] {
        def hasNext = resultSet.next()

        def next() = resultSet
      }.toStream
    }

    def toStream(limit:Int): Stream[ResultSet] = {
      new Iterator[ResultSet] {
        var count: Int =0

        def hasNext = {
          if (count < limit){
            count +=1
            resultSet.next()
          }else{
            false
          }
        }
        def next() = resultSet
      }.toStream
    }
  }
}