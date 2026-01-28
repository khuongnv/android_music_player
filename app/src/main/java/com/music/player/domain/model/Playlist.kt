package com.music.player.domain.model

/**
 * Domain model representing a playlist
 */
data class Playlist(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val songs: List<Song> = emptyList()
) {
    val songCount: Int get() = songs.size
    
    val totalDuration: Long get() = songs.sumOf { it.duration }
    
    val totalDurationFormatted: String
        get() {
            val totalMinutes = (totalDuration / 1000) / 60
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return if (hours > 0) {
                String.format("%d hr %d min", hours, minutes)
            } else {
                String.format("%d min", minutes)
            }
        }
}
