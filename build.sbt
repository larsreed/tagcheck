name := "tagcheck"

version := "1.0"

scalaVersion := "2.11.4"

scalacOptions += "-feature"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.1"
)

libraryDependencies ++= Seq( // test
  "junit" % "junit" % "4.12" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.hamcrest" % "hamcrest-core" % "1.1" % "test",
  "org.specs2" %% "specs2" % "3.3.1" % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test"
)
