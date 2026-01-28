package com.music.player.util

import java.util.Locale

object TimeUtils {
    /**
     * Formats milliseconds to mm:ss format
     */
    fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
