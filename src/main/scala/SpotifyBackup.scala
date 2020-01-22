import java.io.{BufferedWriter, File, FileWriter}
import java.time.LocalDateTime
import java.util.concurrent.Executors

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.{Paging, SavedTrack}

import scala.compat.java8.FutureConverters._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try
import scala.concurrent.duration._


object SpotifyBackup extends App {
  val accessToken = "-"

  implicit val executionCtx = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

  val spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build


    val fut = for {
      tracks: List[SavedTrack] <- findAll()
      success = writeToFile(tracks)
    } yield (success)

  fut.onComplete(_ => executionCtx.shutdown())


  def findAll(offset: Int = 0, tracks: List[SavedTrack] = Nil): Future[List[SavedTrack]] = {
    if (offset == 0) print("Fetching from Spotify: ")
    val pagingFuture: Future[Paging[SavedTrack]] = toScala(spotifyApi.getUsersSavedTracks.offset(offset).build.executeAsync)
    val offsetTracks: Future[(Int, Int, List[SavedTrack])] = for {
      paging <- pagingFuture
      items = paging.getItems.toList
      newOffset = paging.getOffset + paging.getLimit
      total = paging.getTotal
    } yield (newOffset, total, items)


    offsetTracks.flatMap {
      case (newOffset, total, items) => {
        print(".")
        if (offset + 20 > total) {
          Future(items ++ tracks)
        } else {
          findAll(newOffset, items ++ tracks)
        }
      }
      case _ => Future(tracks)
    }.recoverWith {
      case ioe => {
        ioe.printStackTrace
        throw ioe
      }
    }
  }

  def writeToFile(lines: List[SavedTrack]) = {
    print("\nWriting file: ")
    // FileWriter
    val file = new File(LocalDateTime.now().toString + ".csv")
    val bw = new BufferedWriter(new FileWriter(file))
    lines.map(t =>
      s"${t.getTrack.getArtists.map(_.getName).mkString(",")};${t.getTrack.getAlbum.getName};${t.getTrack.getName}\n"
    ).zipWithIndex.foreach {
      case (l: String, i: Int) => {
        val writeEither = Try(bw.write(l)).toEither
        writeEither match {
          case Left(err) => err.printStackTrace
          case _ => print(".")
        }
      }
    }

    bw.close()
    println(s"\n✅ Wrote ${lines.length} lines")
  }

}
