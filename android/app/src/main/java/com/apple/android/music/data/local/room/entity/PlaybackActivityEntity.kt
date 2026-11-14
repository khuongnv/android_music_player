package com.apple.android.music.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity recording playback activity sessions for the 24-hour timeline.
 */
@Entity(
    tableName = "playback_activity",
    indices = [
        Index(value = ["startTime"]),
        Index(value = ["endTime"])
    ]
)
data class PlaybackActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val songTitle: String,
    val artistName: String,
    val albumName: String,
    val artworkUri: String?,
    val songUri: String,
    val songDuration: Long,
    val startPosition: Long,
    val endPosition: Long,
    val startTime: Long,
    val endTime: Long
)
