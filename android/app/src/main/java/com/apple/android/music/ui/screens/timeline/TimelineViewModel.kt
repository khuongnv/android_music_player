package com.apple.android.music.ui.screens.timeline

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.android.music.domain.model.PlaybackActivity
import com.apple.android.music.domain.model.Song
import com.apple.android.music.domain.repository.MusicRepository
import com.apple.android.music.domain.repository.TimelineRepository
import com.apple.android.music.player.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository,
    private val musicRepository: MusicRepository,
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private companion object {
        const val TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000L
    }

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        loadTimelineActivities()
    }

    fun loadTimelineActivities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            timelineRepository.ensureSampleActivitiesIfEmpty()
            
            timelineRepository.getActivitiesLast24Hours().collect { list ->
                val now = System.currentTimeMillis()
                val start = now - TWENTY_FOUR_HOURS_MS
                val totalActiveDuration = list.sumOf { it.activeDurationMs }

                _uiState.update { state ->
                    val fraction = state.scrubFraction
                    val currentSelectedTime = start + (fraction * TWENTY_FOUR_HOURS_MS).toLong()
                    val activeAtPoint = list.firstOrNull { 
                        it.startTime <= currentSelectedTime && currentSelectedTime <= it.endTime 
                    }
                    val pos = activeAtPoint?.getEstimatedPositionAt(currentSelectedTime) ?: 0L

                    state.copy(
                        activities = list,
                        isLoading = false,
                        timelineStartTimestamp = start,
                        timelineEndTimestamp = now,
                        selectedTimestamp = currentSelectedTime,
                        activeItemAtCursor = activeAtPoint,
                        positionAtCursorMs = pos,
                        isActiveAtCursor = activeAtPoint != null,
                        totalListeningTimeMs = totalActiveDuration,
                        activeSessionCount = list.size
                    )
                }
            }
        }
    }

    fun onScrubFractionChanged(fraction: Float) {
        val clamped = fraction.coerceIn(0.0f, 1.0f)
        val start = _uiState.value.timelineStartTimestamp
        val selectedTime = start + (clamped * TWENTY_FOUR_HOURS_MS).toLong()
        val list = _uiState.value.activities

        val activeAtPoint = list.firstOrNull { 
            it.startTime <= selectedTime && selectedTime <= it.endTime 
        }
        val pos = activeAtPoint?.getEstimatedPositionAt(selectedTime) ?: 0L

        _uiState.update {
            it.copy(
                scrubFraction = clamped,
                selectedTimestamp = selectedTime,
                activeItemAtCursor = activeAtPoint,
                positionAtCursorMs = pos,
                isActiveAtCursor = activeAtPoint != null
            )
        }
    }

    fun playAtSelectedPoint() {
        val activeItem = _uiState.value.activeItemAtCursor ?: return
        val positionMs = _uiState.value.positionAtCursorMs

        viewModelScope.launch {
            val songToPlay = resolveSong(
                songId = activeItem.songId,
                title = activeItem.songTitle,
                artist = activeItem.artistName,
                album = activeItem.albumName,
                fallbackUri = activeItem.songUri,
                artworkUri = activeItem.artworkUri,
                duration = activeItem.songDuration
            )

            if (songToPlay != null && songToPlay.uri != Uri.EMPTY) {
                musicServiceConnection.playSongAtPosition(songToPlay, positionMs)
                
                val posMins = (positionMs / 1000) / 60
                val posSecs = (positionMs / 1000) % 60
                val formattedPos = String.format(Locale.US, "%02d:%02d", posMins, posSecs)
                
                _uiState.update {
                    it.copy(
                        feedbackMessage = "Đang phát '${songToPlay.title}' từ đoạn $formattedPos"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        feedbackMessage = "Không tìm thấy tệp nhạc cho '${activeItem.songTitle}' trên thiết bị"
                    )
                }
            }
        }
    }

    fun playSpecificActivity(activity: PlaybackActivity) {
        val positionMs = activity.startPosition
        viewModelScope.launch {
            val songToPlay = resolveSong(
                songId = activity.songId,
                title = activity.songTitle,
                artist = activity.artistName,
                album = activity.albumName,
                fallbackUri = activity.songUri,
                artworkUri = activity.artworkUri,
                duration = activity.songDuration
            )

            if (songToPlay != null && songToPlay.uri != Uri.EMPTY) {
                musicServiceConnection.playSongAtPosition(songToPlay, positionMs)
                val posMins = (positionMs / 1000) / 60
                val posSecs = (positionMs / 1000) % 60
                val formattedPos = String.format(Locale.US, "%02d:%02d", posMins, posSecs)
                _uiState.update {
                    it.copy(feedbackMessage = "Đang phát '${songToPlay.title}' từ đoạn $formattedPos")
                }
            } else {
                _uiState.update {
                    it.copy(feedbackMessage = "Không tìm thấy tệp nhạc '${activity.songTitle}'")
                }
            }
        }
    }

    private suspend fun resolveSong(
        songId: Long,
        title: String,
        artist: String,
        album: String,
        fallbackUri: Uri,
        artworkUri: Uri?,
        duration: Long
    ): Song? {
        // 1. Try finding by ID from Room database
        val songById = musicRepository.getSongById(songId)
        if (songById != null && songById.uri != Uri.EMPTY) {
            return songById
        }

        // 2. Try finding by matching title and artist from library
        val allSongs = musicRepository.getAllSongs()
        val songByTitle = allSongs.firstOrNull { 
            it.title.equals(title, ignoreCase = true) && 
            (it.artist.equals(artist, ignoreCase = true) || artist == "Unknown Artist")
        } ?: allSongs.firstOrNull {
            it.title.equals(title, ignoreCase = true)
        }
        if (songByTitle != null && songByTitle.uri != Uri.EMPTY) {
            return songByTitle
        }

        // 3. Fallback: If fallbackUri is valid and non-empty
        if (fallbackUri != Uri.EMPTY && fallbackUri.toString().isNotBlank()) {
            return Song(
                id = songId,
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                uri = fallbackUri,
                albumId = -1L,
                albumArtUri = artworkUri
            )
        }

        // 4. Fallback: If library is not empty, check partial title match
        val partialMatch = allSongs.firstOrNull {
            it.title.contains(title, ignoreCase = true) || title.contains(it.title, ignoreCase = true)
        }
        if (partialMatch != null && partialMatch.uri != Uri.EMPTY) {
            return partialMatch
        }

        return null
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
