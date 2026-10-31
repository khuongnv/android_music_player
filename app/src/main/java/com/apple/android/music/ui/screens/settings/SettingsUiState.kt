package com.apple.android.music.ui.screens.settings

import com.apple.android.music.domain.model.UpdateStatus

data class FolderInfo(
    val uriString: String,
    val name: String,
    val path: String,
    val isSelected: Boolean = true
)

data class SettingsUiState(
    // Folders & Library
    val folders: List<FolderInfo> = emptyList(),
    val isScanning: Boolean = false,
    
    // Sleep Timer
    val sleepTimerEnabled: Boolean = false,
    val sleepTimerDurationMinutes: Int = 30,
    val sleepTimerTargetTime: Long = 0L,
    val sleepTimerRemainingSeconds: Long = 0L,
    val sleepTimerEndTimeString: String? = null,
    
    // Cache & Storage
    val cacheSizeBytes: Long = 0L,
    val cacheSizeFormatted: String = "0 MB",
    val isClearingCache: Boolean = false,
    val showClearCacheConfirm: Boolean = false,
    
    // App Updates
    val currentVersion: String = "1.0.1",
    val updateStatus: UpdateStatus = UpdateStatus.Idle,
    val showUpdateDialog: Boolean = false,
    
    // General Alerts & Toasts
    val toastMessage: String? = null
)
