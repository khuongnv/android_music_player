package com.apple.android.music.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.android.music.domain.model.PlaybackActivity
import com.apple.android.music.domain.model.Song
import com.apple.android.music.domain.repository.TimelineRepository
import com.apple.android.music.player.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository,
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

        val song = Song(
            id = activeItem.songId,
            title = activeItem.songTitle,
            artist = activeItem.artistName,
            album = activeItem.albumName,
            duration = activeItem.songDuration,
            uri = activeItem.songUri,
            albumId = -1L,
            albumArtUri = activeItem.artworkUri
        )

        musicServiceConnection.playSongAtPosition(song, positionMs)
        
        val posMins = (positionMs / 1000) / 60
        val posSecs = (positionMs / 1000) % 60
        val formattedPos = String.format(java.util.Locale.US, "%02d:%02d", posMins, posSecs)
        
        _uiState.update {
            it.copy(
                feedbackMessage = "Đang phát '${activeItem.songTitle}' từ đoạn $formattedPos"
            )
        }
    }

    fun playSpecificActivity(activity: PlaybackActivity) {
        val song = Song(
            id = activity.songId,
            title = activity.songTitle,
            artist = activity.artistName,
            album = activity.albumName,
            duration = activity.songDuration,
            uri = activity.songUri,
            albumId = -1L,
            albumArtUri = activity.artworkUri
        )
        musicServiceConnection.playSongAtPosition(song, activity.startPosition)
        _uiState.update {
            it.copy(feedbackMessage = "Đang phát '${activity.songTitle}'")
        }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
