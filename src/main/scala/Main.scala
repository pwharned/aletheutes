
import scala.util.{Failure, Success}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import database.{ConcreteDatabaseConfiguration, DB2, DatabaseConnection}
import evaluators.{AccuracyDriftEvaluator, DataDriftEvaluator, ExplainabilityEvaluator, ExplanationResult, ImpactEvaluator}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt
import akka.stream.scaladsl.Source
import spray.json.DefaultJsonProtocol.jsonFormat2


object Main extends App {

  val currentDirectory = new java.io.File(".").getCanonicalPath

  print(currentDirectory +   "/project/database.json")

  val configuraiton = ConcreteDatabaseConfiguration( currentDirectory +   "/project/database.json")

  implicit val connection = DatabaseConnection(configuraiton)

  trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val fairnessFormat = jsonFormat4(FairnessRequest)
    implicit val explainFormat = jsonFormat7(ExplainabilityRequest)
    implicit val driftFormat = jsonFormat5(DriftRequest)
    implicit val accuracyDriftFormat = jsonFormat7(AccuracyDriftRequest)
    implicit val resultFormat = jsonFormat5(Result)


  }



  implicit val jsonStreamingSupport: JsonEntityStreamingSupport
  = EntityStreamingSupport.json()

  case class Result(prediction: String, sex: String, group:Float,disparate_impact: Double,time: String )

  case class FairnessRequest(prediction: String, table: String, protected_column: String, scoring_timestamp: String )

  case class ExplainabilityRequest(table_name: String, target: String, features: Seq[String], id_column: String, max_iter: String, learn_rate: String, ids: Seq[Int])

  case class DriftRequest(table_name: String, features: Seq[String], scoring_timestamp: String, measure: String , over: String)
  case class AccuracyDriftRequest(table_name: String, features: Seq[String], scoring_timestamp: String, learn_rate: String , max_iter: String, target:String,over: String)


  class Application extends Directives with JsonSupport {

    implicit val actorSystem = ActorSystem(Behaviors.empty, "akka-http")


    val disparateImpact = post {

      path("api" / "disparate_impact") {
            entity(as[FairnessRequest]){
              request => onComplete(new ImpactEvaluator.Impact[Result, DB2](request.prediction, request.table, request.protected_column, request.scoring_timestamp).result.flatMap.map(x => json.Json(x.toSeq.asInstanceOf[Seq[Product]]).toString)) {

                  case Success(result) =>  println(result); complete(HttpEntity(ContentTypes.`application/json`, result ))
                  case Failure(ex) =>  println(ex); complete(HttpEntity(ContentTypes.`application/json`, ex.toString ))
                }
            }
      }
    }

    val disparateImpactStream = post {

      path("api" / "disparate_impact_stream") {
        entity(as[FairnessRequest]){
          request => onComplete(new ImpactEvaluator.Impact[Result, DB2](request.prediction, request.table, request.protected_column, request.scoring_timestamp).result.streamToFlatMap.map(x => Source(x))) {
            case Success(result) => complete(result)
            case Failure(ex) => println(ex); complete(HttpEntity(ContentTypes.`application/json`, ex.toString))
          }
        }
      }
    }

    val explainability = post {

      path("api" / "explainability") {

        entity(as[ExplainabilityRequest]){
          request => onComplete(new ExplainabilityEvaluator.Explanation[ExplanationResult, DB2]( request.table_name, request.target, features = request.features, id_column=request.id_column, max_iter = request.max_iter, learn_rate = request.learn_rate,connection = connection, ids = request.ids).retrieve.map(x => json.Json(x.toSeq.asInstanceOf[Seq[Product]]).toString)){

            case Success(x) => println(x); complete( HttpEntity(ContentTypes.`application/json`, x ))
            case Failure(ex) => print(ex);complete(HttpEntity(ContentTypes.`application/json`, ex.toString ))

          }
        }
      }
    }


    val drift = post {

      path("api" / "drift") {

        entity(as[DriftRequest]){
          request => onComplete( DataDriftEvaluator.main( request.table_name, request.features, request.scoring_timestamp, request.measure, request.over, connection = connection).map(x => json.Json(x.toSeq.asInstanceOf[Seq[Product]]).toString)){

            case Success(x) => println(x); complete( HttpEntity(ContentTypes.`application/json`, x ))
            case Failure(ex) => print(ex);complete(HttpEntity(ContentTypes.`application/json`, ex.toString ))

          }
        }
      }
    }
    val accuracyDrift = post {

      path("api" / "drift" / "accuracy") {

        entity(as[AccuracyDriftRequest]){
          request => onComplete( AccuracyDriftEvaluator.main(table_name = request.table_name, features = request.features, scoring_timestamp = request.scoring_timestamp, learn_rate = request.learn_rate, max_iter = request.max_iter,target=request.target, over = request.over, connection = connection).map(x => json.Json(x.toSeq.asInstanceOf[Seq[Product]]).toString)){

            case Success(x) => println(x); complete( HttpEntity(ContentTypes.`application/json`, x ))
            case Failure(ex) => print(ex);complete(HttpEntity(ContentTypes.`application/json`, ex.toString ))

          }
        }
      }
    }


    val routes = disparateImpact ~ explainability ~drift ~accuracyDrift ~disparateImpactStream

    def main = Http().newServerAt("127.0.0.1", 8080).bind(routes)

  }

  val server = new Application
  server.main


}

