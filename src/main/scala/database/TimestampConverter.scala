package database

import java.text.SimpleDateFormat
import java.time.Instant

object TimestampConverter {

  implicit class LongToTimestamp(unixTime: Long){

    def toTimestamp: String = {
      val df:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S")
      df.format(unixTime)

    }
  }

  implicit class TimestamptoLong(unixTime: String){
    def toLongStamp: Long = {
      Instant.parse(unixTime).getEpochSecond
    }
  }



}
