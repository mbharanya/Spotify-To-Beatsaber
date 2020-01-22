name := "spotify-2-beatsaber"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "se.michaelthelin.spotify" % "spotify-web-api-java" % "4.0.2"
libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.9.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % "test"
libraryDependencies += "io.argonaut" %% "argonaut" % "6.2.3"
libraryDependencies += "com.github.vickumar1981" %% "stringdistance" % "1.1.4"
libraryDependencies += "commons-io" % "commons-io" % "2.6"



resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")
