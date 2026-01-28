package com.music.player.ui.screens.search

import com.music.player.domain.model.Album
import com.music.player.domain.model.Artist
import com.music.player.domain.model.Song

data class SearchUiState(
    val query: String = "",
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
