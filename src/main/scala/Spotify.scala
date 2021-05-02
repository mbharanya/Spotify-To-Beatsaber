
import java.util.concurrent.Executors
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.{Paging, Playlist, PlaylistSimplified, SavedTrack}

import scala.compat.java8.FutureConverters.{toScala, _}
import scala.concurrent.{ExecutionContext, Future}

class Spotify(accessToken: String) {
  val spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build
  implicit val executionCtx = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

  def findAllSavedTracks(): Future[List[SavedTrack]] = {
    val pagingFuture: Future[Paging[SavedTrack]] = toScala(spotifyApi.getUsersSavedTracks.offset(0).build.executeAsync)
    fetchAllRecursively(pagingFuture)
  }

  private def fetchAllRecursively[T](pagingFuture: Future[Paging[T]], offset: Int = 0, fetchedItems: List[T] = Nil): Future[List[T]] = {
    val offsetTracks = for {
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
          Future(items ++ fetchedItems)
        } else {
          fetchAllRecursively(pagingFuture, newOffset, items ++ fetchedItems)
        }
      }
      case _ => {
        println()
        Future(fetchedItems)
      }
    }.recoverWith {
      case ioe => {
        ioe.printStackTrace
        throw ioe
      }
    }
  }

  def getCurrentUser() = {
    toScala(spotifyApi.getCurrentUsersProfile().build().executeAsync())
  }
  def getCurrentUsersPlaylists() = {
    for {
      user <- getCurrentUser()
      playlists <- getAllUsersPlaylists(user.getId)
    } yield playlists
  }

  def getAllUsersPlaylists(userId: String): Future[List[PlaylistSimplified]] = {
    val pagingFuture: Future[Paging[PlaylistSimplified]] = toScala(spotifyApi.getListOfUsersPlaylists(userId).build().executeAsync)
    fetchAllRecursively(pagingFuture)
  }

  def getCurrentUsersPlaylistByName(playlistName: String) = {
    for {
      user <- getCurrentUser()
      playlists <- getUserPlaylistByName(user.getId, playlistName)
    } yield playlists
  }

  def getUserPlaylistByName(userId: String, playlistName: String) = {
    getAllUsersPlaylists(userId).map(_.filter(_.getName == playlistName).headOption)
  }
}
