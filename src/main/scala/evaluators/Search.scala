package evaluators

import database._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.runtime.universe._

object Search {



    def result = Future(Array("drift", "accuracy", "disparate_impact"))




}


