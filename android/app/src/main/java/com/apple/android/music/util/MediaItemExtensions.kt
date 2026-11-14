package com.apple.android.music.util

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.apple.android.music.domain.model.Song

/**
 * Extension functions to convert between Song domain model and Media3 MediaItem.
 */
fun Song.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setRequestMetadata(
            MediaItem.RequestMetadata.Builder()
                .setMediaUri(uri)
                .build()
        )
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(albumArtUri)
                .build()
        )
        .build()
}

fun MediaItem.toSong(duration: Long = 0L): Song? {
    val metadata = mediaMetadata
    return try {
        Song(
            id = mediaId.toLong(),
            title = metadata.title?.toString() ?: "Unknown",
            artist = metadata.artist?.toString() ?: "Unknown Artist",
            album = metadata.albumTitle?.toString() ?: "Unknown Album",
            duration = duration,
            uri = localConfiguration?.uri ?: requestMetadata.mediaUri ?: Uri.EMPTY,
            albumId = 0L,
            albumArtUri = metadata.artworkUri
        )
    } catch (e: Exception) {
        null
    }
}
