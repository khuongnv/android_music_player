package com.apple.android.music.domain.model

data class AppUpdateInfo(
    val currentVersion: String,
    val newVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val assetSize: Long,
    val isUpdateAvailable: Boolean
)
