import java.net.URLEncoder

import argonaut.Parse

import argonaut._, Argonaut._

import scala.io.Source

trait SongResource {
  def find(artist: String, song: String): Either[String, SearchResult]
}

case class SearchResult(name: String, downloads: Long, downloadUrl: String) {
}


object BeatsaverSongResource extends SongResource {
  val searchUrl = "https://beatsaver.com/api/search/text/0?q=$SEARCH"

  override def find(artist: String, song: String) = {
    val result = Source.fromURL(
      searchUrl.replace("$SEARCH", URLEncoder.encode(s"${artist} ${song}", "UTF-8"))
    ).mkString

    val searchResult = Parse.parse(result) match {
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