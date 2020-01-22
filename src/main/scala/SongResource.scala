import java.net.URLEncoder

import argonaut.Parse
import argonaut._
import Argonaut._

import scala.io.Source
import scala.util.Try

trait SongResource {
  def find(artist: String, song: String): Either[String, SearchResult]
}

case class SearchResult(name: String, downloads: Long, downloadUrl: String) {
}


object BeatsaverSongResource extends SongResource {
  val searchUrl = "https://beatsaver.com/api/search/text/0?q=$SEARCH"

  override def find(artist: String, song: String) = {
    println(s"Searching for ${artist} ${song}")
    val result = Try(Source.fromURL(
      searchUrl.replace("$SEARCH", URLEncoder.encode(s"${artist} ${song}", "UTF-8"))
    )).toEither

    result match {
      case Left(err) => {
        System.err.println(err.printStackTrace)
        Left(err.getMessage)
      }
      case Right(bufferedSource) => {
        val searchResult = Parse.parse(bufferedSource.mkString) match {
          case Left(error) => Left(error)
          case Right(json) => {
            val docs = (json.hcursor --\ "docs").downArray

            val maybeSearchResult = for {
              name <- (docs --\ "name").focus
              downloads <- (docs --\ "stats" --\ "downloads").focus
              downloadUrl <- (docs --\ "directDownload").focus
            } yield
              SearchResult(
                name.stringOrEmpty,
                downloads.numberOrZero.toLong.getOrElse(0l),
                s"https://beatsaver.com${downloadUrl.stringOrEmpty}"
              )

            maybeSearchResult match {
              case Some(searchResult) => Right(searchResult)
              case None => Left("Did not find any results")
            }
          }
        }

        searchResult
      }
    }

  }


}