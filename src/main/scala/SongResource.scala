import java.io.Writer
import argonaut.Parse
import sttp.client3._

trait SongResource {
  def find(artist: String, song: String)(implicit stringDistance: ((String, String) => Double)): Either[String, SearchResult]
}

case class SearchResult(name: String, downloads: Long, downloadUrl: String, confidence: Double) {
}


class BeatsaverSongResource(val csvWriter: Writer) extends SongResource {
  csvWriter.write("\"Artist\";\"Song\";\"fullName\";\"songName\";\"songSubName\";\"songAuthorName\";\"confidence\"\n")

  override def find(inArtist: String, inSong: String)(implicit stringDistance: ((String, String) => Double)) = {
    println(s"Searching for ${inArtist} ${inSong}")

    Thread.sleep(100)

    val artist = inArtist.toLowerCase
    val song = inSong.toLowerCase

    val request = basicRequest.get(uri"https://beatsaver.com/api/search/text/0?q=$artist $song")
      .header("authority","beatsaver.com")
      .header("pragma","no-cache")
      .header("cache-control","no-cache")
      .header("sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"90\", \"Google Chrome\";v=\"90\"")
      .header("sec-ch-ua-mobile","?0")
      .header("dnt","1")
      .header("upgrade-insecure-requests","1")
      .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0")
      .header("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("sec-fetch-site","none")
      .header("sec-fetch-mode","navigate")
      .header("sec-fetch-user","?1")
      .header("sec-fetch-dest","document")
      .header("accept-language","en-US,en;q=0.9")

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