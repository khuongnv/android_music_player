package com.music.player.ui.screens.library

import com.music.player.domain.model.Album
import com.music.player.domain.model.Artist
import com.music.player.domain.model.Playlist
import com.music.player.domain.model.Song

/**
 * UI state for LibraryScreen
 */
data class LibraryUiState(
    val isLoading: Boolean = false,
    val isFirstLoad: Boolean = true,
    val songs: List<Song> = emptyList(),
    val recentAlbums: List<Album> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val selectedTab: LibraryTab = LibraryTab.Songs,
    val sortOrder: SongSortOrder = SongSortOrder.NAME_ASC,
    val searchQuery: String = "",
    val error: String? = null
)

enum class LibraryTab {
    Songs, Albums, Artists, Playlists
}

enum class SongSortOrder {
    NAME_ASC, NAME_DESC
}
