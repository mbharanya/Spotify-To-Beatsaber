import java.io.{PrintWriter, Writer}
import java.net.{URL, URLEncoder}
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import argonaut.{ACursor, Parse}
import org.apache.commons.io.IOUtils

import scala.util.Try

trait SongResource {
  def find(artist: String, song: String)(implicit stringDistance: ((String, String) => Double)): Either[String, SearchResult]
}

case class SearchResult(name: String, downloads: Long, downloadUrl: String, confidence: Double) {
}


class BeatsaverSongResource(val csvWriter: Writer) extends SongResource {
  val searchUrl = "https://beatsaver.com/api/search/text/0?q=$SEARCH"
  csvWriter.write("\"Artist\";\"Song\";\"fullName\";\"songName\";\"songSubName\";\"songAuthorName\";\"confidence\"\n")

  override def find(inArtist: String, inSong: String)(implicit stringDistance: ((String, String) => Double)) = {
    println(s"Searching for ${inArtist} ${inSong}")

    Thread.sleep(100)

    val artist = inArtist.toLowerCase
    val song = inSong.toLowerCase

    val result = Try(
      IOUtils.toString(new URL(
        searchUrl.replace("$SEARCH", URLEncoder.encode(s"${artist} ${song}", "UTF-8"))
      ), java.nio.charset.StandardCharsets.UTF_8)
    ).toEither

    result match {
      case Left(err) => {
        System.err.println(err.printStackTrace)
        Left(err.getMessage)
      }
      case Right(jsonBuffer) => {
        val searchResult = Parse.parse(jsonBuffer) match {
          case Left(error) => Left(error)
          case Right(json) => {
            val docs = (json.hcursor --\ "docs").downArray

            val maybeSearchResult = for {
              fullName <- (docs --\ "name").focus
              songName <- (docs --\ "metadata" --\ "songName").focus
              songSubName <- (docs --\ "metadata" --\ "songSubName").focus
              songAuthorName <- (docs --\ "metadata" --\ "songAuthorName").focus
              downloads <- (docs --\ "stats" --\ "downloads").focus
              downloadUrl <- (docs --\ "directDownload").focus
            } yield {
              val fullNameS = fullName.stringOrEmpty.toLowerCase
              val songNameS = songName.stringOrEmpty.toLowerCase
              val songSubNameS = songSubName.stringOrEmpty.toLowerCase
              val songAuthorNameS = songAuthorName.stringOrEmpty.toLowerCase

              val scores = Seq(
                stringDistance(s"${artist} ${song}", fullNameS),
                stringDistance(s"${song}", fullNameS),

                stringDistance(s"${artist} ${song}", songNameS),
                stringDistance(s"${song}", songNameS),

                stringDistance(artist, songSubNameS),

                stringDistance(artist, songAuthorNameS)
              )

              val confidence = (scores.sum / scores.length.toDouble)


              SearchResult(
                fullNameS,
                downloads.numberOrZero.toLong.getOrElse(0l),
                s"https://beatsaver.com${downloadUrl.stringOrEmpty}",
                confidence
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