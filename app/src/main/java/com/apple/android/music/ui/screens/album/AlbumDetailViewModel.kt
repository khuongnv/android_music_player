package com.apple.android.music.ui.screens.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.android.music.domain.model.Song
import com.apple.android.music.domain.repository.MusicRepository
import com.apple.android.music.player.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicServiceConnection: MusicServiceConnection,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val albumId: Long = savedStateHandle.get<Long>("albumId") ?: -1L

    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    val currentSong = musicServiceConnection.currentSong

    init {
        loadAlbumDetails()
    }

    fun loadAlbumDetails() {
        if (albumId == -1L) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val album = musicRepository.getAlbumById(albumId)
                val songs = musicRepository.getSongsForAlbum(albumId)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        album = album,
                        songs = songs
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load album details"
                    )
                }
            }
        }
    }

    fun playAll() {
        if (_uiState.value.songs.isNotEmpty()) {
            musicServiceConnection.playSongs(_uiState.value.songs, 0)
        }
    }

    fun shufflePlay() {
        if (_uiState.value.songs.isNotEmpty()) {
            musicServiceConnection.toggleShuffle()
            // In a real app, we might shuffle the list manually first or just play with shuffle enabled
            musicServiceConnection.playSongs(_uiState.value.songs, 0)
        }
    }

    fun playSong(song: Song) {
        val index = _uiState.value.songs.indexOf(song)
        if (index >= 0) {
            musicServiceConnection.playSongs(_uiState.value.songs, index)
        }
    }
}
