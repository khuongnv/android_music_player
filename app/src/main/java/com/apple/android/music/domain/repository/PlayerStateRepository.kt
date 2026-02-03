package com.apple.android.music.domain.repository

import com.apple.android.music.domain.model.PlayerState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for saving and retrieving player state.
 */
interface PlayerStateRepository {
    val playerState: Flow<PlayerState>
    suspend fun savePlayerState(state: PlayerState)
    suspend fun getLatestState(): PlayerState
}
