import java.net.URLEncoder

import argonaut.Parse
import argonaut._
import Argonaut._
import com.github.vickumar1981.stringdistance.StringDistance.Levenshtein

import scala.io.Source
import scala.util.Try

trait SongResource {
  def find(artist: String, song: String): Either[String, SearchResult]
}

case class SearchResult(name: String, downloads: Long, downloadUrl: String, confidence: Double) {
}


object BeatsaverSongResource extends SongResource {
  val searchUrl = "https://beatsaver.com/api/search/text/0?q=$SEARCH"

  override def find(inArtist: String, inSong: String) = {
    println(s"Searching for ${inArtist} ${inSong}")

    val artist = inArtist.toLowerCase
    val song = inSong.toLowerCase

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
            } yield {
              val foundSongName = name.stringOrEmpty.toLowerCase
              val scoreArtistSong = Levenshtein.score(s"${artist} ${song}", foundSongName)
              val scoreSong = Levenshtein.score(s"${song}", foundSongName)

              val maxScore = Math.max(scoreArtistSong, scoreSong)

              SearchResult(
                foundSongName,
                downloads.numberOrZero.toLong.getOrElse(0l),
                s"https://beatsaver.com${downloadUrl.stringOrEmpty}",
                maxScore
              )
            }

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