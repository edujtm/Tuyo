package me.edujtm.tuyo.data.endpoint

import com.google.api.services.youtube.YouTube
import me.edujtm.tuyo.data.model.PrimaryPlaylistsIds
import javax.inject.Inject

class YoutubeUserEndpoint
    @Inject constructor(val youtube: YouTube) : UserEndpoint {

    override fun getPrimaryPlaylistsIds(): PrimaryPlaylistsIds = youtube.channels()
        .list("snippet,contentDetails,statistics")
        .apply {
            mine = true
        }.execute()
        .items
        .firstOrNull()?.let {
            val playlists = it.contentDetails.relatedPlaylists
            PrimaryPlaylistsIds(
                likedVideos = playlists.likes,
                favorites = playlists.favorites,
                watchLater = playlists.watchLater,
                history = playlists.watchHistory
            )
        } ?: throw Exception("Couldn't retrieve primary user playlists")

}