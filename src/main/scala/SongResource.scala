import java.net.URLEncoder

import argonaut.Parse

import argonaut._, Argonaut._

import scala.io.Source

trait SongResource {
  def findDownloadUrl(artist: String, song: String): SearchResult
}

case class SearchResult(name: String, downloads: Long, downloadUrl: String){
}


object BeatsaverSongResource extends SongResource {
  val searchUrl = "https://beatsaver.com/api/search/text/0?q=$SEARCH"

  override def findDownloadUrl(artist: String, song: String): SearchResult = {
    val result = Source.fromURL(
      searchUrl.replace("$SEARCH", URLEncoder.encode(s"${artist} ${song}", "UTF-8"))
    ).mkString

    val searchResult: SearchResult = Parse.parse(result) match {
      case Left(error) => throw new IllegalStateException(error)
      case Right(json) => {
        val docs = (json.hcursor --\ "docs").downArray
        val name = (docs --\ "name").focus
        val downloads = (docs --\ "stats" --\ "downloads").focus
        val downloadUrl = (docs --\ "directDownload").focus
        SearchResult(
          name.map(_.stringOrEmpty).getOrElse(""),
          downloads.map(_.numberOrZero.toLong.get).getOrElse(0l),
          s"https://beatsaver.com${downloadUrl.map(_.stringOrEmpty).getOrElse("")}"
        )
      }
    }

    searchResult
  }


}