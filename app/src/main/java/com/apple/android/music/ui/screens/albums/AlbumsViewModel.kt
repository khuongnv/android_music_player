package com.apple.android.music.ui.screens.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.android.music.domain.model.Album
import com.apple.android.music.domain.usecase.GetAlbumsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    getAlbumsUseCase: GetAlbumsUseCase
) : ViewModel() {

    val albumsState: StateFlow<AlbumsUiState> = getAlbumsUseCase()
        .map { albums -> AlbumsUiState(albums = albums) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlbumsUiState(isLoading = true)
        )
}

data class AlbumsUiState(
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = false
)
