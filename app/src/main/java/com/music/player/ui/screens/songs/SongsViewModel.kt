package com.music.player.ui.screens.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.player.domain.model.Song
import com.music.player.domain.usecase.GetSongsUseCase
import com.music.player.player.MusicServiceConnection
import com.music.player.ui.screens.library.SongSortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor(
    getSongsUseCase: GetSongsUseCase,
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SongSortOrder.NAME_ASC)
    private val _searchQuery = MutableStateFlow("")

    val songsState: StateFlow<SongsUiState> = combine(
        getSongsUseCase(),
        _sortOrder,
        _searchQuery
    ) { songs, sortOrder, query ->
        val filtered = if (query.isEmpty()) songs else {
            songs.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.artist.contains(query, ignoreCase = true) 
            }
        }
        val sorted = when (sortOrder) {
            SongSortOrder.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
            SongSortOrder.NAME_DESC -> filtered.sortedByDescending { it.title.lowercase() }
        }
        SongsUiState(songs = sorted)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SongsUiState(isLoading = true)
    )

    val currentSong = musicServiceConnection.currentSong
    val isPlaying = musicServiceConnection.isPlaying

    fun playSong(song: Song) {
        val currentList = songsState.value.songs
        if (currentList.isNotEmpty()) {
            val index = currentList.indexOf(song)
            if (index != -1) {
                musicServiceConnection.playSongs(currentList, index)
            }
        }
    }
    
    fun shuffleAll() {
        val currentList = songsState.value.songs
        if (currentList.isNotEmpty()) {
            musicServiceConnection.playSongs(currentList.shuffled(), 0)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}

data class SongsUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false
)
