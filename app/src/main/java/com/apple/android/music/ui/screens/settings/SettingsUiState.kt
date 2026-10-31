package com.apple.android.music.ui.screens.settings

import com.apple.android.music.domain.model.UpdateStatus

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
    val sleepTimerEndTime: String? = null,
    val currentVersion: String = "1.0.0",
    val updateStatus: UpdateStatus = UpdateStatus.Idle,
    val showUpdateDialog: Boolean = false
)
