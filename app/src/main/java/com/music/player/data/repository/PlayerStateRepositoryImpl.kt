package com.music.player.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.music.player.domain.model.PlayerState
import com.music.player.domain.repository.PlayerStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "player_state")

@Singleton
class PlayerStateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PlayerStateRepository {

    private object PreferencesKeys {
        val SONG_ID = longPreferencesKey("song_id")
        val QUEUE_IDS = stringPreferencesKey("queue_ids")
        val PLAYLIST_ID = longPreferencesKey("playlist_id")
        val POSITION = longPreferencesKey("position")
        val IS_PLAYING = booleanPreferencesKey("is_playing")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val LAST_SAVE_TIMESTAMP = longPreferencesKey("last_save_timestamp")
    }

    override val playerState: Flow<PlayerState> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapPlayerState(preferences)
        }

    override suspend fun savePlayerState(state: PlayerState) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SONG_ID] = state.songId
            preferences[PreferencesKeys.QUEUE_IDS] = state.queueIds.joinToString(",")
            preferences[PreferencesKeys.PLAYLIST_ID] = state.playlistId
            preferences[PreferencesKeys.POSITION] = state.position
            preferences[PreferencesKeys.IS_PLAYING] = state.isPlaying
            preferences[PreferencesKeys.REPEAT_MODE] = state.repeatMode
            preferences[PreferencesKeys.SHUFFLE_MODE] = state.shuffleModeEnabled
            preferences[PreferencesKeys.PLAYBACK_SPEED] = state.playbackSpeed
            preferences[PreferencesKeys.LAST_SAVE_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    override suspend fun getLatestState(): PlayerState {
        return playerState.first()
    }

    private fun mapPlayerState(preferences: Preferences): PlayerState {
        val queueString = preferences[PreferencesKeys.QUEUE_IDS] ?: ""
        val queueIds = if (queueString.isNotEmpty()) {
            queueString.split(",").mapNotNull { it.toLongOrNull() }
        } else {
            emptyList()
        }

        return PlayerState(
            songId = preferences[PreferencesKeys.SONG_ID] ?: -1L,
            queueIds = queueIds,
            playlistId = preferences[PreferencesKeys.PLAYLIST_ID] ?: -1L,
            position = preferences[PreferencesKeys.POSITION] ?: 0L,
            isPlaying = preferences[PreferencesKeys.IS_PLAYING] ?: false,
            repeatMode = preferences[PreferencesKeys.REPEAT_MODE] ?: 0,
            shuffleModeEnabled = preferences[PreferencesKeys.SHUFFLE_MODE] ?: false,
            playbackSpeed = preferences[PreferencesKeys.PLAYBACK_SPEED] ?: 1.0f,
            lastSaveTimestamp = preferences[PreferencesKeys.LAST_SAVE_TIMESTAMP] ?: 0L
        )
    }
}
