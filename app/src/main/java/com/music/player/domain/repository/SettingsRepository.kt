package com.music.player.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val selectedFolders: Flow<Set<String>>
    suspend fun setSelectedFolders(folders: Set<String>)
    suspend fun getSelectedFolders(): Set<String>

    val sleepTimerEnabled: Flow<Boolean>
    suspend fun setSleepTimerEnabled(enabled: Boolean)

    val sleepTimerDuration: Flow<Int>
    suspend fun setSleepTimerDuration(minutes: Int)
}
