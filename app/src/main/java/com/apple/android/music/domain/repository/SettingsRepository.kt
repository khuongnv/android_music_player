package com.apple.android.music.domain.repository

import com.apple.android.music.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val selectedFolders: Flow<Set<String>>
    suspend fun setSelectedFolders(folders: Set<String>)
    suspend fun getSelectedFolders(): Set<String>

    val sleepTimerEnabled: Flow<Boolean>
    suspend fun setSleepTimerEnabled(enabled: Boolean)

    val sleepTimerDuration: Flow<Int>
    suspend fun setSleepTimerDuration(minutes: Int)

    val sleepTimerTargetTime: Flow<Long>
    suspend fun setSleepTimer(durationMinutes: Int)
    suspend fun cancelSleepTimer()

    suspend fun getCacheSizeBytes(): Long
    suspend fun clearCache(): Boolean

    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
