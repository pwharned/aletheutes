package database


import java.lang.Class
import database.Clause
import json.Json
import json.Json.JsonMap
import database.Driver
import scala.io.Source
import java.sql.{Connection, DriverManager, ResultSet}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.reflect.runtime.universe.TypeTag

trait Configuration

object Configuration {
  def fromJson(string: String): Map[String, String] = {
    JsonMap.fromString(string).toMap

  }

  def fromFile(path: String): Map[String, String] = {
    val fileContents  = Source.fromFile(path).getLines.map(_.trim).mkString("")
    this.fromJson(fileContents)

  }

  def apply(path: String): Map[String, String] = {

    this.fromFile(path)
  }
}



abstract class AbstractDatabaseConfiguration(host: String, port: String, username: String, password: String, driver: String, database: String) extends Configuration {

  def getUsername: String = this.username

  def getPassword: String = this.password

  def connectionString: String = {
    val username = this.username
    val password = this.password
    val host = this.host
    val port = this.port
    val driver = this.getDriver.name
    val database = this.database
    f"jdbc:$driver%s://$host%s:$port%s/$database%s"
  }


  def getDriver: Driver = {
    Driver(this.driver)

  }

  def getType: String = {
    Driver(this.driver).name
  }

}

class ConcreteDatabaseConfiguration(host: String, port: String, username: String, password: String, driver: String, database: String) extends AbstractDatabaseConfiguration(host: String, port: String, username: String, password: String, driver: String, database: String)


object ConcreteDatabaseConfiguration extends Configuration {

  def apply(host: String, port: String, username: String, password: String, driver: String, database: String): ConcreteDatabaseConfiguration = new ConcreteDatabaseConfiguration(host, port, username, password, driver, database)

  def apply(map: Map[String, String]): ConcreteDatabaseConfiguration = {

    new ConcreteDatabaseConfiguration(host = map("host"), port =  map("port"), password = map("password"), username = map("username"), driver = map("driver"), database = map("database"))
  }

  def apply(path: String): ConcreteDatabaseConfiguration = {
    ConcreteDatabaseConfiguration(Configuration(path))

  }
}

sealed trait DB2

abstract class AbstractDatabaseConnection[A](configuration: AbstractDatabaseConfiguration) {

  def connection: Future[Connection] = Future {
    //Class.forName( configuration.getDriver.toString)
    DriverManager.getConnection(configuration.connectionString, configuration.getUsername, configuration.getPassword )

  }

  def execute(query: String): Future[ResultSet] = connection.map(x => { val statement = x.prepareStatement(query);statement.setFetchSize(100);statement.executeQuery() } )



}

class DB2DatabaseConnection(configuration: AbstractDatabaseConfiguration) extends AbstractDatabaseConnection[DB2](configuration: AbstractDatabaseConfiguration)


object DatabaseConnection {
  def apply(configuration: AbstractDatabaseConfiguration): AbstractDatabaseConnection[DB2] = configuration.getType match {
    case "db2" => new DB2DatabaseConnection(configuration)
  }
}






