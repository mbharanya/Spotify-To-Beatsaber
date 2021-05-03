import java.io.{File, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import com.github.vickumar1981.stringdistance.StringDistance.{Levenshtein, NGram}
import com.wrapper.spotify.model_objects.IModelObject
import com.wrapper.spotify.model_objects.specification.SavedTrack
import org.backuity.clist.{CliMain, arg, args, opt}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}
import scala.util.Try

object SpotifyToBeatsaber extends CliMain[Unit](
  name = "Spotify-To-Beatsaber",
  description = "Play your spotify songs in BeatSaber!") {
  implicit val executionCtx = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))
  val csvWriter = new PrintWriter(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).replaceAll(":", ".") + "-matches.csv")
  val minConfidence = 0.42
  implicit val stringDistance: (String, String) => Double = Levenshtein.score


  var apiKey = opt[Option[String]](description = "Spotify API key, Get it from https://developer.spotify.com/console/get-current-user-saved-tracks/")
  var downloadFolder = opt[String](default = ".", description = "folder to download the songs zips, default is current directory")
  var mode = opt[String](default = "all_tracks", description = "all_tracks | playlist")
  var playlistName = opt[Option[String]](description = "if mode is set to playlist, download songs from that playlist instead")

  def run: Unit = {
    val usage =
      """
        |Play your spotify songs in BeatSaber!
        |--api-key <Spotify API key, Get it from https://developer.spotify.com/console/get-current-user-saved-tracks/>
        |--download-folder <folder to download the songs zips, default is current directory>
        |--mode <all_tracks | playlist>
        |[--playlist-name] <if mode is set to playlist, download songs from that playlist instead>
        |""".stripMargin


    case class Config(apiKey: String, downloadFolder: String)
    val spotify = new Spotify(apiKey.getOrElse("Api key is required"))

    val songsToDownload = if (mode == "playlist") {
      spotify.getPlaylistTracks(playlistName.getOrElse(throw new IllegalStateException("Playlist name must be set in 'playlist' mode")))
    } else {
      spotify.findAllSavedTracks()
    }

    val statsF = for {
      tracks <- songsToDownload
    } yield download(downloadFolder, tracks)

    writeCsv(statsF)
  }


  private def download(downloadFolder: String, tracks: List[Song]) = {
    val songResource: SongResource = new BeatsaverSongResource(csvWriter)

    tracks.map { song =>
      val searchResult = songResource.find(song)
      val matches = searchResult match {
        case Left(error) => {
          System.err.println(s"Could not find ${song.artist} ${song.name}: ${error}")
          (1, 0)
        }
        case Right(result) => {
          if (result.confidence < minConfidence) {
            System.err.println(s"'${result.name}' is not close enough to '${song.artist} ${song.name}' - ${result.confidence}")
            (1, 0)
          } else {
            println(s"Found song '${song.artist} ${song.name}' <-> '${result.name}' (confidence ${result.confidence}), ${result.downloads} downloads, ${result.downloadUrl}")
            val fileName = result.name.replaceAll("\\W+", "-")
            Downloader.download(result.downloadUrl, s"${downloadFolder}${File.separator}${fileName}.zip")
            (0, 1)
          }
        }
      }
      println("\n")
      matches
    }
  }

  private def writeCsv(statsF: Future[List[(Int, Int)]]) = {
    for {
      stats <- statsF
    } yield {
      val (failed, succeeded) = stats.fold((0, 0))((a: (Int, Int), b: (Int, Int)) => {
        ((a._1 + b._1), a._2 + b._2)
      })
      csvWriter.close()

      println(s"✅ Downloaded ${succeeded} / ${succeeded + failed} (${Math.round((succeeded.toDouble / (failed.toDouble + succeeded.toDouble)) * 100)}%)")
      executionCtx.shutdown()
      System.exit(0)
    }
  }
}

