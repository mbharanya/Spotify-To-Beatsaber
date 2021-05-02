
import java.util.concurrent.Executors
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.{Paging, Playlist, PlaylistSimplified, SavedTrack, Track}
import pb.ProgressBar

import scala.compat.java8.FutureConverters.{toScala, _}
import scala.concurrent.{ExecutionContext, Future}

class Spotify(accessToken: String) {
  val spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build
  implicit val executionCtx = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

  def findAllSavedTracks(): Future[List[Song]] = {
    val pagingFutureF = (offset: Int) => toScala(spotifyApi.getUsersSavedTracks.offset(offset).build.executeAsync)
    fetchAllRecursively(pagingFutureF).map(savedTracks => savedTracks.map(st => toSong(st.getTrack)))
  }

  def toSong(track: Track) = Song(
    artist = track.getArtists()(0).getName,
    name = track.getName
  )

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
    val pagingFutureF = (offset: Int) => toScala(spotifyApi.getListOfUsersPlaylists(userId).offset(offset).build().executeAsync)
    fetchAllRecursively(pagingFutureF)
  }

  def getCurrentUsersPlaylistByName(playlistName: String) = {
    for {
      user <- getCurrentUser()
      playlists <- getUserPlaylistByName(user.getId, playlistName)
    } yield playlists
  }

  def getUserPlaylistByName(userId: String, playlistName: String) = {
    getAllUsersPlaylists(userId).map(_.filter(_.getName.toLowerCase == playlistName.toLowerCase).headOption)
  }

  def getPlaylistTracks(playlistName: String) = {
    FutureFlattenOps.flattenList(for {
      playlist <- getCurrentUsersPlaylistByName(playlistName)
    } yield playlist
      .map(_.getId)
      .map(id => (offset:Int) => toScala(spotifyApi.getPlaylistsItems(id).offset(offset).build().executeAsync))
      .map(fetchAllRecursively(_))
    ).map(playlistTracks => {
        playlistTracks.map(track => {
          val trackObj = track.getTrack().asInstanceOf[Track]
          toSong(trackObj)
        })
    })
  }

  private def fetchAllRecursively[T](pagingFutureF: Int => Future[Paging[T]], offset: Int = 0, fetchedItems: List[T] = Nil): Future[List[T]] = {
    val pagingFuture = pagingFutureF(offset)
    val offsetTracks = for {
      paging <- pagingFuture
      items = paging.getItems.toList
      newOffset = paging.getOffset + paging.getLimit
      total = paging.getTotal.toInt
    } yield (newOffset, total, items)

    offsetTracks.flatMap {
      case (newOffset, total, items) => {
        println("Fetching songs from Spotify")
        var progressBar = new ProgressBar(total)
        progressBar += newOffset

        if (offset + 20 > total) {
          println()
          Future(items ++ fetchedItems)
        } else {
          fetchAllRecursively(pagingFutureF, newOffset, items ++ fetchedItems)
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
}
