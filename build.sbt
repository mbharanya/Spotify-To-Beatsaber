name := "spotify-2-beatsaber"

version := "0.3"

scalaVersion := "2.13.5"

libraryDependencies += "se.michaelthelin.spotify" % "spotify-web-api-java" % "6.5.4"
// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-java8-compat
libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % "test"
libraryDependencies += "io.argonaut" %% "argonaut" % "6.2.3"
libraryDependencies += "com.github.vickumar1981" %% "stringdistance" % "1.2.2"
libraryDependencies += "commons-io" % "commons-io" % "2.6"
libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.3.0"
libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.3.0"
libraryDependencies ++= Seq(
  "org.backuity.clist" %% "clist-core" % "3.5.1",
  "org.backuity.clist" %% "clist-macros" % "3.5.1" % "provided"
)

lazy val root = (project in file(".")).dependsOn(scalaProgressBar)

lazy val scalaProgressBar = ProjectRef(uri("git://github.com/a8m/pb-scala.git#master"), "pb-scala")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}


resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")