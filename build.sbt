name := "aletheutes"

version := "0.1"

scalaVersion := "2.13.6"


libraryDependencies += "com.ibm.db2.jcc" % "db2jcc" % "db2jcc4"

libraryDependencies += "org.scala-lang"  % "scala-reflect" % scalaVersion.value
libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.13.6"
libraryDependencies += "com.typesafe.akka"% "akka-actor-typed_2.13" % "2.6.14"
libraryDependencies += "com.typesafe.akka"% "akka-stream-typed_2.13" % "2.6.14"
libraryDependencies += "com.typesafe.akka"% "akka-http_2.13" % "10.2.4"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.4"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.14"

excludeFilter in (Compile, unmanagedSources) := {
  val test = ((sourceDirectory in Compile).value / "test").getCanonicalPath
  new SimpleFileFilter(_.getCanonicalPath startsWith test)
}


assemblyJarName in assembly := s"application.jar"



mainClass in assembly := Some("Main")
