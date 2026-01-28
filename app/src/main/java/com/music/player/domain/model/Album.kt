package com.music.player.domain.model

import android.net.Uri

/**
 * Domain model representing an album
 */
data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val songCount: Int,
    val albumArtUri: Uri?
)
