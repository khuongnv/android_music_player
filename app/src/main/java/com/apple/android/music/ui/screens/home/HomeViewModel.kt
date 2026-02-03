package com.apple.android.music.ui.screens.home

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
import android.util.Log
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val currentSong = musicServiceConnection.currentSong
    val isPlaying = musicServiceConnection.isPlaying
    val currentPosition = musicServiceConnection.currentPosition
    val duration = musicServiceConnection.duration
    val isConnected = musicServiceConnection.isConnected

    init {
        Log.d("MusicApp", "HomeViewModel init")
        observeMusicData()
        loadHomeData()
        musicServiceConnection.connect()
    }

    private fun observeMusicData() {
        viewModelScope.launch {
            musicRepository.getSongsFlow().collectLatest { songs ->
                _uiState.update { it.copy(recentSongs = songs.take(30)) }
            }
        }
        viewModelScope.launch {
            musicRepository.getAlbumsFlow().collectLatest { albums ->
                _uiState.update { it.copy(recentAlbums = albums.take(15)) }
            }
        }
    }

    fun loadHomeData() {
        Log.d("MusicApp", "HomeViewModel: loadHomeData called")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val songs = musicRepository.getAllSongs()
                val albums = musicRepository.getAllAlbums()
                
                Log.d("MusicApp", "HomeViewModel: Loaded ${songs.size} songs and ${albums.size} albums")
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recentSongs = songs.take(30),
                        recentAlbums = albums.take(15)
                    )
                }
            } catch (e: Exception) {
                Log.e("MusicApp", "HomeViewModel: Failed to load data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load music"
                    )
                }
            }
        }
    }

    fun playSong(song: Song) {
        musicServiceConnection.playSong(song)
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        musicServiceConnection.playSongs(songs, startIndex)
    }

    fun togglePlayPause() {
        musicServiceConnection.togglePlayPause()
    }

    fun playNext() {
        musicServiceConnection.next()
    }

    fun playPrevious() {
        musicServiceConnection.previous()
    }

    fun saveState() {
        musicServiceConnection.saveState()
    }

    override fun onCleared() {
        super.onCleared()
        // Don't disconnect here to keep playback running
    }
}
