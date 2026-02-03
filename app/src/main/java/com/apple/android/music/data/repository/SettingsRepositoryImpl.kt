package com.apple.android.music.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.apple.android.music.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val SELECTED_FOLDERS = stringSetPreferencesKey("selected_folders")
        val SLEEP_TIMER_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("sleep_timer_enabled")
        val SLEEP_TIMER_DURATION = androidx.datastore.preferences.core.intPreferencesKey("sleep_timer_duration")
    }

    override val selectedFolders: Flow<Set<String>> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_FOLDERS] ?: emptySet()
        }

    override suspend fun setSelectedFolders(folders: Set<String>) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_FOLDERS] = folders
        }
    }

    override suspend fun getSelectedFolders(): Set<String> {
        return selectedFolders.first()
    }

    override val sleepTimerEnabled: Flow<Boolean> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.SLEEP_TIMER_ENABLED] ?: false
        }

    override suspend fun setSleepTimerEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.SLEEP_TIMER_ENABLED] = enabled
        }
    }

    override val sleepTimerDuration: Flow<Int> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.SLEEP_TIMER_DURATION] ?: 30
        }

    override suspend fun setSleepTimerDuration(minutes: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.SLEEP_TIMER_DURATION] = minutes
        }
    }
}
