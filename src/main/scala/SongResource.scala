import argonaut.Parse
import com.github.vickumar1981.stringdistance.StringDistance.Levenshtein
import sttp.client._

trait SongResource {
  def find(artist: String, song: String): Either[String, SearchResult]
}

case class SearchResult(name: String, downloads: Long, downloadUrl: String, confidence: Double) {
}

object BeatsaverSongResource extends SongResource {
  override def find(inArtist: String, inSong: String) = {
    println(s"Searching for ${inArtist} ${inSong}")

    val artist = inArtist.toLowerCase
    val song = inSong.toLowerCase


    val request = basicRequest.get(uri"https://beatsaver.com/api/search/text/0?q=$artist $song")
      .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36")

    implicit val backend = HttpURLConnectionBackend()
    val response = request.send()

    response.body match {
      case Left(err) => {
        System.err.println(err)
        Left(err)
      }
      case Right(jsonBuffer) => {
        val searchResult = Parse.parse(jsonBuffer) match {
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