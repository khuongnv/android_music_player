package com.music.player.ui.screens.home

import com.music.player.domain.model.Album
import com.music.player.domain.model.Song

/**
 * UI state for HomeScreen
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val recentSongs: List<Song> = emptyList(),
    val recentAlbums: List<Album> = emptyList(),
    val error: String? = null
)
