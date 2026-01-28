package com.music.player.ui.screens.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.player.data.local.room.entity.PlaylistEntity
import com.music.player.domain.model.Playlist
import com.music.player.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    playlistRepository: PlaylistRepository
) : ViewModel() {

    val playlistsState: StateFlow<PlaylistsUiState> = playlistRepository.getAllPlaylists()
        .map { playlists -> PlaylistsUiState(playlists = playlists) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlaylistsUiState(isLoading = true)
        )
}


data class PlaylistsUiState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false
)
