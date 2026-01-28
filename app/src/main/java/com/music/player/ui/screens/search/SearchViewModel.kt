package com.music.player.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.player.domain.model.Song
import com.music.player.domain.repository.MusicRepository
import com.music.player.player.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val currentSong = musicServiceConnection.currentSong
    val isPlaying = musicServiceConnection.isPlaying

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        if (query.length >= 2) {
            search(query)
        } else {
            _uiState.update { it.copy(songs = emptyList(), albums = emptyList(), artists = emptyList()) }
        }
    }

    private fun search(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val allSongs = musicRepository.getAllSongs()
                val allAlbums = musicRepository.getAllAlbums()
                val allArtists = musicRepository.getAllArtists()

                val filteredSongs = allSongs.filter { 
                    it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true) 
                }
                val filteredAlbums = allAlbums.filter { 
                    it.name.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
                }
                val filteredArtists = allArtists.filter { 
                    it.name.contains(query, ignoreCase = true) 
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        songs = filteredSongs,
                        albums = filteredAlbums,
                        artists = filteredArtists
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
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
}
