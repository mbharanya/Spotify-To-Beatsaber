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

    val statsF = for {
      tracks: List[SavedTrack] <- spotify.findAll()
    } yield tracks.map { savedTrack =>
      val track = savedTrack.getTrack
      val searchResult = songResource.find(track.getArtists()(0).getName, track.getName)
      val matches = searchResult match {
        case Left(error) => {
          System.err.println(s"Could not find ${track.getArtists()(0).getName} ${track.getName}: ${error}")
          (1, 0)
        }
        case Right(result) => {
          if (result.confidence < 0.4) {
            System.err.println(s"'${result.name}' is not close enough to '${track.getArtists()(0).getName} ${track.getName}' - ${result.confidence}")
            (1, 0)
          }else{
            println(s"Found song '${track.getArtists()(0).getName} ${track.getName}' <-> '${result.name}', ${result.downloads} downloads, ${result.downloadUrl}")
            val fileName = result.name.replaceAll("\\W+", "-")
            Downloader.download(result.downloadUrl, s"${downloadFolder}${File.separator}${fileName}.zip")
            (0,1)
          }
        }
      }
      println("\n")
      matches
    }

    for {
      stats <- statsF
    }yield{
      val (failed, succeeded) = stats.fold((0,0))((a: (Int, Int), b: (Int, Int)) => {
        ((a._1 + b._1), a._2 + b._2)
      })

      println(s"✅ Downloaded ${succeeded} / ${succeeded + failed} (${Math.round((succeeded.toDouble / (failed.toDouble + succeeded.toDouble)) * 100)}%)")
      executionCtx.shutdown()
      System.exit(0)
    }
  }




}

