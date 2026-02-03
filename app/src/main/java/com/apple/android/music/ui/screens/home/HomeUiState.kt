package com.apple.android.music.ui.screens.home

import com.apple.android.music.domain.model.Album
import com.apple.android.music.domain.model.Song

/**
 * UI state for HomeScreen
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val recentSongs: List<Song> = emptyList(),
    val recentAlbums: List<Album> = emptyList(),
    val error: String? = null
)
