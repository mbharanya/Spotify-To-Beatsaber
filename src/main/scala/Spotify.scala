
import java.util.concurrent.Executors

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.{Paging, SavedTrack}

import scala.compat.java8.FutureConverters._
import scala.concurrent.{ExecutionContext, Future}

class Spotify(accessToken: String) {

  val spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build
  implicit val executionCtx = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))


  def findAll(offset: Int = 0, tracks: List[SavedTrack] = Nil): Future[List[SavedTrack]] = {
    if (offset == 0) print("Fetching from Spotify: ")
    val pagingFuture: Future[Paging[SavedTrack]] = toScala(spotifyApi.getUsersSavedTracks.offset(offset).build.executeAsync)
    val offsetTracks: Future[(Int, Int, List[SavedTrack])] = for {
      paging <- pagingFuture
      items = paging.getItems.toList
      newOffset = paging.getOffset + paging.getLimit
      total = paging.getTotal.toInt
    } yield (newOffset, total, items)


    offsetTracks.flatMap {
      case (newOffset, total, items) => {
        print(".")
        if (offset + 20 > total) {
          println()
          Future(items ++ tracks)
        } else {
          findAll(newOffset, items ++ tracks)
        }
      }
      case _ => {
        println()
        Future(tracks)
      }
    }.recoverWith {
      case ioe => {
        ioe.printStackTrace
        throw ioe
      }
    }
  }

}
