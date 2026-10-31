package com.apple.android.music.domain.model

data class UpdateDownloadProgress(
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val percent: Int
)
