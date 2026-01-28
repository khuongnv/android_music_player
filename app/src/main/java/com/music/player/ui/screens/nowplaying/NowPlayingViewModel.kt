package com.music.player.ui.screens.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.player.player.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val shuffleEnabled = musicServiceConnection.shuffleEnabled
    val repeatMode = musicServiceConnection.repeatMode

    private val _positionFlow = MutableStateFlow(0L)
    val positionFlow = _positionFlow.asStateFlow()

    init {
        updatePosition()
    }

    private fun updatePosition() {
        viewModelScope.launch {
            while (true) {
                _positionFlow.value = musicServiceConnection.getCurrentPosition()
                delay(1000)
            }
        }
    }

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
        musicServiceConnection.toggleShuffle()
    }

    fun toggleRepeat() {
        musicServiceConnection.toggleRepeat()
    }
}
