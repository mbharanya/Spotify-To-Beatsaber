import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
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
          if (result.confidence < 0.4) {
            System.err.println(s"'${result.name}' is not close enough to '${track.getArtists()(0).getName} ${track.getName}' - ${result.confidence}")
          }else{
            println(s"\nFound song '${track.getArtists()(0).getName} ${track.getName}' <-> '${result.name}', ${result.downloads} downloads, ${result.downloadUrl}")
            val fileName = result.name.replaceAll("\\W+", "-")
            Downloader.download(result.downloadUrl, s"${downloadFolder}${File.separator}${fileName}.zip")
          }
        }
      }
    }
  }




}

