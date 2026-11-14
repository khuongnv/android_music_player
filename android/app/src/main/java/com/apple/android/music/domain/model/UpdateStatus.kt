package com.apple.android.music.domain.model

import java.io.File

sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data class UpdateAvailable(val updateInfo: AppUpdateInfo) : UpdateStatus
    data class UpToDate(val currentVersion: String) : UpdateStatus
    data class Downloading(
        val progressPercent: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : UpdateStatus
    data class ReadyToInstall(val apkFile: File) : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}
