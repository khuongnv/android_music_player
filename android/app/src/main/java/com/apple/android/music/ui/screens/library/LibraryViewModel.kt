package com.apple.android.music.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.android.music.domain.model.Album
import com.apple.android.music.domain.model.Artist
import com.apple.android.music.domain.model.Song
import com.apple.android.music.domain.repository.MusicRepository
import com.apple.android.music.domain.repository.PlaylistRepository
import com.apple.android.music.player.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    val currentSong = musicServiceConnection.currentSong
    val isPlaying = musicServiceConnection.isPlaying

    init {
        observeMusicData()
        observePlaylists()
        loadLibrary()
    }

    private fun observeMusicData() {
        viewModelScope.launch {
            combine(
                musicRepository.getSongsFlow(),
                musicRepository.getAlbumsFlow(),
                musicRepository.getArtistsFlow(),
                _searchQuery,
                _uiState // For sort order
            ) { songs, albums, artists, query, state ->
                // Filter and update state in one go
                val filteredSongs = if (query.isEmpty()) songs else {
                    songs.filter { it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true) }
                }
                val sortedSongs = applySort(filteredSongs, state.sortOrder)
                
                val filteredAlbums = if (query.isEmpty()) albums else {
                    albums.filter { it.name.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true) }
                }
                
                val filteredArtists = if (query.isEmpty()) artists else {
                    artists.filter { it.name.contains(query, ignoreCase = true) }
                }
                
                val recentAlbums = filteredAlbums.sortedByDescending { it.id }.take(10)

                _uiState.update { 
                    it.copy(
                        songs = sortedSongs,
                        albums = filteredAlbums,
                        recentAlbums = recentAlbums,
                        artists = filteredArtists,
                        searchQuery = query,
                        isFirstLoad = false
                    )
                }
            }.collectLatest { }
        }
    }


    fun loadLibrary(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value.isFirstLoad) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            
            try {
                // Just trigger repository refresh, flow will update UI
                musicRepository.refreshMusic()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isFirstLoad = false,
                        error = e.message ?: "Failed to load library"
                    )
                }
            }
        }
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists().collectLatest { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
    }

    fun selectTab(tab: LibraryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleSortOrder() {
        _uiState.update { state ->
            val newOrder = if (state.sortOrder == SongSortOrder.NAME_ASC) {
                SongSortOrder.NAME_DESC
            } else {
                SongSortOrder.NAME_ASC
            }
            state.copy(
                sortOrder = newOrder,
                songs = applySort(state.songs, newOrder)
            )
        }
    }

    private fun applySort(songs: List<Song>, order: SongSortOrder): List<Song> {
        return when (order) {
            SongSortOrder.NAME_ASC -> songs.sortedBy { it.title.lowercase() }
            SongSortOrder.NAME_DESC -> songs.sortedByDescending { it.title.lowercase() }
        }
    }

    fun playSong(song: Song) {
        val songs = _uiState.value.songs
        val index = songs.indexOf(song)
        if (index >= 0) {
            musicServiceConnection.playSongs(songs, index)
        } else {
            musicServiceConnection.playSong(song)
        }
    }

    fun togglePlayPause() {
        musicServiceConnection.togglePlayPause()
    }

    fun playNext() {
        musicServiceConnection.next()
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
        }
    }
}
