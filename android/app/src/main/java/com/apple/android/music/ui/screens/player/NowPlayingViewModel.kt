package com.apple.android.music.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.android.music.domain.model.Song
import com.apple.android.music.player.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    val currentSong = musicServiceConnection.currentSong
    val isPlaying = musicServiceConnection.isPlaying
    val currentPosition = musicServiceConnection.currentPosition
    val duration = musicServiceConnection.duration

    // Simple shuffle/repeat toggle placeholders. 
    // Real implementation depends on ExoPlayer commands or custom logic in Service.
    // Assuming MusicServiceConnection exposes specific methods or we add them.
    // For now, toggleShuffle calls service.shuffle() if it existed, or we implement basic logic.
    // musicServiceConnection.transportControls?.setShuffleMode(...)
    
    fun togglePlayPause() {
        musicServiceConnection.togglePlayPause()
    }

    fun skipNext() {
        musicServiceConnection.next()
    }

    fun skipPrevious() {
        musicServiceConnection.previous()
    }

    fun seekTo(position: Long) {
        musicServiceConnection.seekTo(position)
    }
    
    fun toggleShuffle() {
        // musicServiceConnection.toggleShuffle() // TODO: Implement in Connection/Service
    }
    
    fun toggleRepeat() {
        // musicServiceConnection.toggleRepeat() // TODO: Implement in Connection/Service
    }
}
