package com.apple.android.music.domain.model

import android.net.Uri

/**
 * Domain model representing an active playback activity session.
 */
data class PlaybackActivity(
    val id: Long,
    val songId: Long,
    val songTitle: String,
    val artistName: String,
    val albumName: String,
    val artworkUri: Uri?,
    val songUri: Uri,
    val songDuration: Long,
    val startPosition: Long,
    val endPosition: Long,
    val startTime: Long,
    val endTime: Long
) {
    /**
     * Estimates the position within the song at a specific epoch timestamp.
     */
    fun getEstimatedPositionAt(timestamp: Long): Long {
        if (timestamp <= startTime) return startPosition
        if (timestamp >= endTime) return endPosition
        val durationSpan = (endTime - startTime).coerceAtLeast(1L)
        val progressFraction = (timestamp - startTime).toDouble() / durationSpan
        val estimated = startPosition + ((endPosition - startPosition) * progressFraction).toLong()
        return estimated.coerceIn(0L, if (songDuration > 0) songDuration else Long.MAX_VALUE)
    }

    val activeDurationMs: Long
        get() = (endTime - startTime).coerceAtLeast(0L)
}
