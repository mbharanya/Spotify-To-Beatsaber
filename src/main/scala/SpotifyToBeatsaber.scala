import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream, IOException}
import java.net.URL
import java.util.concurrent.Executors

import com.wrapper.spotify.model_objects.specification.SavedTrack
import org.apache.commons.io.FileUtils

import scala.concurrent.ExecutionContext

object SpotifyToBeatsaber {
  def main(args: Array[String]): Unit = {
    implicit val executionCtx = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

    val accessToken = args(0)
    val spotify = new Spotify(accessToken)

    val songResource: SongResource = BeatsaverSongResource

    val urls = for {
      tracks: List[SavedTrack] <- spotify.findAll()
    } yield tracks.map { savedTrack =>
      val track = savedTrack.getTrack
      val searchResult = songResource.findDownloadUrl(track.getArtists()(0).getName, track.getName)
      println(s"Found song '${track.getArtists()(0).getName} ${track.getName}' <-> '${searchResult.name}', ${searchResult.downloads} downloads, ${searchResult.downloadUrl}")

      downloadFile(searchResult.downloadUrl, filepath = s"/tmp/${searchResult.name}.zip")
      searchResult
    }
  }


  def downloadFile(url: String, filepath: String): Unit = {
    FileUtils.copyURLToFile(new URL(url), new File(filepath))
    println("Downloaded " + filepath)
  }


}

