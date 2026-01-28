package com.music.player.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val selectedFolders: Flow<Set<String>>
    suspend fun setSelectedFolders(folders: Set<String>)
    suspend fun getSelectedFolders(): Set<String>
}
