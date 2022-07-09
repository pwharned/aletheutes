package database


import scala.util.{Success, Try, Failure}


sealed abstract class Driver(val driver: String){
  def name: String
  def getDriver: String = this.driver
  def getInstance: Unit = Class.forName(this.getDriver)
}

case object DB2Driver extends Driver("com.ibm.db2.jcc.DB2Driver"){
  override def name: String = "db2"

  override def toString: String = this.driver


}

case object DriverNotFound extends Driver( "None"){
  def name: String = "None"
}

object Driver {


  private val byDriver: Map[String, Driver] = Map("com.ibm.db2.jcc.DB2Driver"->DB2Driver)
  private val byName: Map[String, String] = Map("db2" -> "com.ibm.db2.jcc.DB2Driver")

  def getByName(driverName: String): Driver = this.byName.get(driverName) match {
    case Some(v) => this.getByDriver(v)
    case None => DriverNotFound
  }

  def getByDriver(driverName: String): Driver = {
    this.byDriver.get(driverName) match {
      case Some(driver) => driver
      case None => DriverNotFound
    }
  }

  def apply(driver: String): Driver = {
    this.getByDriver(driver) match {
      case DriverNotFound => this.getByName(driver)
      case x: Driver=> x
    }
  }



}


