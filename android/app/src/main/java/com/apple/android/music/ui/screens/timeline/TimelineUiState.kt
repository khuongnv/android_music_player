package com.apple.android.music.ui.screens.timeline

import com.apple.android.music.domain.model.PlaybackActivity

data class TimelineUiState(
    val activities: List<PlaybackActivity> = emptyList(),
    val isLoading: Boolean = false,
    val timelineStartTimestamp: Long = 0L,
    val timelineEndTimestamp: Long = 0L,
    val scrubFraction: Float = 1.0f, // 0.0f (24h ago) to 1.0f (now)
    val selectedTimestamp: Long = 0L,
    val activeItemAtCursor: PlaybackActivity? = null,
    val positionAtCursorMs: Long = 0L,
    val isActiveAtCursor: Boolean = false,
    val totalListeningTimeMs: Long = 0L,
    val activeSessionCount: Int = 0,
    val feedbackMessage: String? = null
)
