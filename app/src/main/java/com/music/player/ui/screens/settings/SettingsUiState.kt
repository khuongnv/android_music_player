package com.music.player.ui.screens.settings

data class FolderInfo(
    val path: String,
    val name: String,
    val isSelected: Boolean
)

data class SettingsUiState(
    val folders: List<FolderInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sleepTimerEnabled: Boolean = false,
    val sleepTimerDuration: String = "30",
    val sleepTimerEndTime: String? = null
)
