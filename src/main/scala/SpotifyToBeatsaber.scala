import java.io.File
import java.util.concurrent.Executors

import com.wrapper.spotify.model_objects.specification.SavedTrack

import scala.concurrent.ExecutionContext
import scala.util.Try

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
          fileDownloader(result.downloadUrl, s"${downloadFolder}${File.separator}${result.name}.zip")
        }
      }
    }
  }

  import java.io.File
  import java.net.URL

  import sys.process._

  def fileDownloader(url: String, filename: String) = {
    new URL(url) #> new File(filename) !!;
    println(s"Downloaded ${new File(filename).getAbsolutePath}")
  }


}

