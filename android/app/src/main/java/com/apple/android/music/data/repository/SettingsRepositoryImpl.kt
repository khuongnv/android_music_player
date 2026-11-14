package com.apple.android.music.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import coil.Coil
import com.apple.android.music.domain.model.ThemeMode
import com.apple.android.music.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
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
        val SLEEP_TIMER_ENABLED = booleanPreferencesKey("sleep_timer_enabled")
        val SLEEP_TIMER_DURATION = intPreferencesKey("sleep_timer_duration")
        val SLEEP_TIMER_TARGET_TIME = longPreferencesKey("sleep_timer_target_time")
        val THEME_MODE = stringPreferencesKey("theme_mode")
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
            if (!enabled) {
                preferences[PreferencesKeys.SLEEP_TIMER_TARGET_TIME] = 0L
            }
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

    override val sleepTimerTargetTime: Flow<Long> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.SLEEP_TIMER_TARGET_TIME] ?: 0L
        }

    override suspend fun setSleepTimer(durationMinutes: Int) {
        val targetTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.SLEEP_TIMER_ENABLED] = true
            preferences[PreferencesKeys.SLEEP_TIMER_DURATION] = durationMinutes
            preferences[PreferencesKeys.SLEEP_TIMER_TARGET_TIME] = targetTime
        }
    }

    override suspend fun cancelSleepTimer() {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.SLEEP_TIMER_ENABLED] = false
            preferences[PreferencesKeys.SLEEP_TIMER_TARGET_TIME] = 0L
        }
    }

    override suspend fun getCacheSizeBytes(): Long = withContext(Dispatchers.IO) {
        var totalSize = 0L
        try {
            totalSize += getDirectorySize(context.cacheDir)
            context.externalCacheDir?.let {
                totalSize += getDirectorySize(it)
            }
        } catch (e: Exception) {
            // Ignore error reading cache size
        }
        totalSize
    }

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    override suspend fun clearCache(): Boolean = withContext(Dispatchers.IO) {
        var success = true
        try {
            // 1. Clear Coil caches
            try {
                Coil.imageLoader(context).diskCache?.clear()
                Coil.imageLoader(context).memoryCache?.clear()
            } catch (e: Exception) {
                success = false
            }

            // 2. Clear app internal cache directory
            context.cacheDir?.listFiles()?.forEach { file ->
                try {
                    file.deleteRecursively()
                } catch (e: Exception) {
                    success = false
                }
            }

            // 3. Clear external cache directory
            context.externalCacheDir?.listFiles()?.forEach { file ->
                try {
                    file.deleteRecursively()
                } catch (e: Exception) {
                    // Non-critical
                }
            }
        } catch (e: Exception) {
            success = false
        }
        success
    }

    override val themeMode: Flow<ThemeMode> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val raw = preferences[PreferencesKeys.THEME_MODE]
            try {
                if (raw != null) ThemeMode.valueOf(raw) else ThemeMode.SYSTEM
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    private fun getDirectorySize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            size += if (file.isDirectory) {
                getDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }
}
