package com.apple.android.music.domain.model

/**
 * Domain model representing an artist
 */
data class Artist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val albumCount: Int = 0
)
