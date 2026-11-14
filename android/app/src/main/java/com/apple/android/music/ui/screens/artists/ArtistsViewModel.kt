package com.apple.android.music.ui.screens.artists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.android.music.domain.model.Artist
import com.apple.android.music.domain.usecase.GetArtistsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    getArtistsUseCase: GetArtistsUseCase
) : ViewModel() {

    val artistsState: StateFlow<ArtistsUiState> = getArtistsUseCase()
        .map { artists -> ArtistsUiState(artists = artists) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ArtistsUiState(isLoading = true)
        )
}

data class ArtistsUiState(
    val artists: List<Artist> = emptyList(),
    val isLoading: Boolean = false
)
