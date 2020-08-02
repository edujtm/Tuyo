package me.edujtm.tuyo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_items")
data class PlaylistItem(
    @PrimaryKey
    val id: String,
    val channelId: String,
    val title: String,
    val description: String,
    val playlistId: String,
    val videoId: String,
    val thumbnail: String?,
    val nextPageKey: String?
) {
    companion object {
        // TODO: Maybe change this to a better abstraction
        fun fromJson(
            json: com.google.api.services.youtube.model.PlaylistItem,
            nextToken: String? = null
        ) : PlaylistItem {
            val snippet = json.snippet
            return PlaylistItem(
                json.id,
                snippet.channelId,
                snippet.title,
                snippet.description,
                snippet.playlistId,
                json.contentDetails.videoId,
                snippet.thumbnails?.default?.url,
                nextToken
            )
        }
    }
}
