package com.apple.android.music.ui.screens.search

import com.apple.android.music.domain.model.Album
import com.apple.android.music.domain.model.Artist
import com.apple.android.music.domain.model.Song

data class SearchUiState(
    val query: String = "",
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
