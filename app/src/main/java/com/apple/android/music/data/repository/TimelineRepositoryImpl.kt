package com.apple.android.music.data.repository

import android.net.Uri
import com.apple.android.music.data.local.room.dao.PlaybackActivityDao
import com.apple.android.music.data.local.room.dao.SongDao
import com.apple.android.music.data.local.room.entity.PlaybackActivityEntity
import com.apple.android.music.domain.model.PlaybackActivity
import com.apple.android.music.domain.repository.TimelineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimelineRepositoryImpl @Inject constructor(
    private val playbackActivityDao: PlaybackActivityDao,
    private val songDao: SongDao
) : TimelineRepository {

    private companion object {
        const val TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000L
        const val SESSION_CONTINUATION_WINDOW_MS = 30_000L // 30 seconds
    }

    override fun getActivitiesLast24Hours(): Flow<List<PlaybackActivity>> {
        val since = System.currentTimeMillis() - TWENTY_FOUR_HOURS_MS
        return playbackActivityDao.getActivitiesSinceFlow(since).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getActivitiesLast24HoursSnapshot(): List<PlaybackActivity> = withContext(Dispatchers.IO) {
        val since = System.currentTimeMillis() - TWENTY_FOUR_HOURS_MS
        playbackActivityDao.getActivitiesSince(since).map { it.toDomain() }
    }

    override suspend fun recordActivity(
        songId: Long,
        songTitle: String,
        artistName: String,
        albumName: String,
        artworkUri: String?,
        songUri: String,
        songDuration: Long,
        currentPosition: Long,
        timestamp: Long
    ) = withContext(Dispatchers.IO) {
        val latest = playbackActivityDao.getLatestActivity()
        
        if (latest != null && 
            latest.songId == songId && 
            (timestamp - latest.endTime) < SESSION_CONTINUATION_WINDOW_MS
        ) {
            // Extend existing activity segment
            val updated = latest.copy(
                endTime = timestamp,
                endPosition = currentPosition.coerceAtLeast(latest.startPosition)
            )
            playbackActivityDao.update(updated)
        } else {
            // New activity session
            val newEntity = PlaybackActivityEntity(
                songId = songId,
                songTitle = songTitle,
                artistName = artistName,
                albumName = albumName,
                artworkUri = artworkUri,
                songUri = songUri,
                songDuration = songDuration,
                startPosition = currentPosition,
                endPosition = currentPosition,
                startTime = timestamp,
                endTime = timestamp + 1000L // initial small duration
            )
            playbackActivityDao.insert(newEntity)

            // Opportunistically prune activities older than 48 hours
            val pruneBefore = timestamp - (TWENTY_FOUR_HOURS_MS * 2)
            playbackActivityDao.deleteOldActivities(pruneBefore)
        }
    }

    override suspend fun closeActiveSession() = withContext(Dispatchers.IO) {
        // Nothing mandatory; natural break occurs on next session
    }

    override suspend fun ensureSampleActivitiesIfEmpty() = withContext(Dispatchers.IO) {
        val count = playbackActivityDao.getActivityCount()
        if (count > 0) return@withContext

        val allSongs = songDao.getAllSongs().first()
        if (allSongs.isEmpty()) return@withContext

        val now = System.currentTimeMillis()

        // Create 3 realistic listening blocks over the last 24h
        // Session 1: 16h ago (Morning)
        var cursorTime = now - (16 * 60 * 60 * 1000L)
        val morningSongs = allSongs.take(3)
        for (song in morningSongs) {
            val dur = if (song.duration > 0) song.duration else 180_000L
            val playDur = dur.coerceAtMost(240_000L)
            playbackActivityDao.insert(
                PlaybackActivityEntity(
                    songId = song.id,
                    songTitle = song.title,
                    artistName = song.artist,
                    albumName = song.album,
                    artworkUri = song.artworkUri,
                    songUri = song.path,
                    songDuration = dur,
                    startPosition = 0L,
                    endPosition = playDur,
                    startTime = cursorTime,
                    endTime = cursorTime + playDur
                )
            )
            cursorTime += playDur + 5_000L
        }

        // Session 2: 7h ago (Afternoon)
        cursorTime = now - (7 * 60 * 60 * 1000L)
        val afternoonSongs = allSongs.shuffled().take(4)
        for (song in afternoonSongs) {
            val dur = if (song.duration > 0) song.duration else 210_000L
            val playDur = dur.coerceAtMost(220_000L)
            playbackActivityDao.insert(
                PlaybackActivityEntity(
                    songId = song.id,
                    songTitle = song.title,
                    artistName = song.artist,
                    albumName = song.album,
                    artworkUri = song.artworkUri,
                    songUri = song.path,
                    songDuration = dur,
                    startPosition = 0L,
                    endPosition = playDur,
                    startTime = cursorTime,
                    endTime = cursorTime + playDur
                )
            )
            cursorTime += playDur + 8_000L
        }

        // Session 3: 1h ago (Recent)
        cursorTime = now - (65 * 60 * 1000L)
        val recentSongs = allSongs.takeLast(2)
        for (song in recentSongs) {
            val dur = if (song.duration > 0) song.duration else 190_000L
            val playDur = dur.coerceAtMost(190_000L)
            playbackActivityDao.insert(
                PlaybackActivityEntity(
                    songId = song.id,
                    songTitle = song.title,
                    artistName = song.artist,
                    albumName = song.album,
                    artworkUri = song.artworkUri,
                    songUri = song.path,
                    songDuration = dur,
                    startPosition = 0L,
                    endPosition = playDur,
                    startTime = cursorTime,
                    endTime = cursorTime + playDur
                )
            )
            cursorTime += playDur + 3_000L
        }
    }

    private fun PlaybackActivityEntity.toDomain(): PlaybackActivity {
        return PlaybackActivity(
            id = id,
            songId = songId,
            songTitle = songTitle,
            artistName = artistName,
            albumName = albumName,
            artworkUri = artworkUri?.let { Uri.parse(it) },
            songUri = Uri.parse(songUri),
            songDuration = songDuration,
            startPosition = startPosition,
            endPosition = endPosition,
            startTime = startTime,
            endTime = endTime
        )
    }
}
