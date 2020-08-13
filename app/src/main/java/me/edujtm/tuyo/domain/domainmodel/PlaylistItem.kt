package me.edujtm.tuyo.domain.domainmodel


data class PlaylistItem(
    val id: String,
    val channelId: String,
    val title: String,
    val description: String,
    val playlistId: String,
    val videoId: String,
    val thumbnailUrl: String?,
    val nextPageToken: String?
)

