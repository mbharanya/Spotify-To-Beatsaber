import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.concurrent.Executors

import com.wrapper.spotify.model_objects.specification.SavedTrack

import scala.concurrent.ExecutionContext
import scala.util.Try
import scalaj.http._

object SpotifyToBeatsaber {
  def main(args: Array[String]): Unit = {
    implicit val executionCtx = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

    val accessToken = Try(args(0)).toOption.getOrElse(throw new IllegalArgumentException("Please add your spotify token as the first argument. Get it from https://developer.spotify.com/console/get-current-user-saved-tracks/"))
    val downloadFolder = Try(args(1)).toOption.getOrElse(".")

    val spotify = new Spotify(accessToken)

    val songResource: SongResource = BeatsaverSongResource

    for {
      tracks: List[SavedTrack] <- spotify.findAll()
    } yield tracks.map { savedTrack =>
      val track = savedTrack.getTrack
      val searchResult = songResource.find(track.getArtists()(0).getName, track.getName)
      searchResult match {
        case Left(error) => System.err.println(s"Could not find ${track.getArtists()(0).getName} ${track.getName}: ${error}")
        case Right(result) => {
          println(s"\nFound song '${track.getArtists()(0).getName} ${track.getName}' <-> '${result.name}', ${result.downloads} downloads, ${result.downloadUrl}")
          download(result.downloadUrl, s"${downloadFolder}${File.separator}${result.name}.zip")
        }
      }
    }
  }


  def download(url: String, filename: String) ={
    val response: HttpResponse[String] = Http(url).header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:72.0) Gecko/20100101 Firefox/72.0").asString
    Files.write(new File(filename).toPath, response.body.getBytes(StandardCharsets.UTF_8))
    println(s"Downloaded ${new File(filename).getAbsolutePath}")
  }

}

