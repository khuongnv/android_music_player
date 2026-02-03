package com.apple.android.music.ui.screens.album

import com.apple.android.music.domain.model.Album
import com.apple.android.music.domain.model.Song

/**
 * UI state for AlbumDetailScreen
 */
data class AlbumDetailUiState(
    val isLoading: Boolean = true,
    val album: Album? = null,
    val songs: List<Song> = emptyList(),
    val error: String? = null
)
