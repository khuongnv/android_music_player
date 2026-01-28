package com.music.player.ui.screens.album

import com.music.player.domain.model.Album
import com.music.player.domain.model.Song

/**
 * UI state for AlbumDetailScreen
 */
data class AlbumDetailUiState(
    val isLoading: Boolean = true,
    val album: Album? = null,
    val songs: List<Song> = emptyList(),
    val error: String? = null
)
