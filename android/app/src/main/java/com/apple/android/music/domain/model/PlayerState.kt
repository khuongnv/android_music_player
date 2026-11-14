package com.apple.android.music.domain.model

/**
 * Data model for preserving the player state across app restarts.
 */
data class PlayerState(
    val songId: Long = -1L,
    val queueIds: List<Long> = emptyList(),
    val playlistId: Long = -1L,
    val position: Long = 0L,
    val isPlaying: Boolean = false,
    val repeatMode: Int = 0, // Player.REPEAT_MODE_OFF
    val shuffleModeEnabled: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val lastSaveTimestamp: Long = 0L
)
