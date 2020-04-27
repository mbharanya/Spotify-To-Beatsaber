name := "spotify-2-beatsaber"

version := "0.2"

scalaVersion := "2.12.10"

libraryDependencies += "se.michaelthelin.spotify" % "spotify-web-api-java" % "4.0.2"
libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.9.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % "test"
libraryDependencies += "io.argonaut" %% "argonaut" % "6.2.3"
libraryDependencies += "com.github.vickumar1981" %% "stringdistance" % "1.2.2"
libraryDependencies += "commons-io" % "commons-io" % "2.6"
libraryDependencies += "com.softwaremill.sttp.client" %% "core" % "2.1.0-RC1"


resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")
