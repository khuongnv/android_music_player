package com.apple.android.music.domain.repository

import com.apple.android.music.domain.model.PlaybackActivity
import kotlinx.coroutines.flow.Flow

interface TimelineRepository {
    fun getActivitiesLast24Hours(): Flow<List<PlaybackActivity>>
    suspend fun getActivitiesLast24HoursSnapshot(): List<PlaybackActivity>
    suspend fun recordActivity(
        songId: Long,
        songTitle: String,
        artistName: String,
        albumName: String,
        artworkUri: String?,
        songUri: String,
        songDuration: Long,
        currentPosition: Long,
        timestamp: Long = System.currentTimeMillis()
    )
    suspend fun closeActiveSession()
    suspend fun ensureSampleActivitiesIfEmpty()
}
