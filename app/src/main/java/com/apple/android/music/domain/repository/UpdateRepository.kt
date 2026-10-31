package com.apple.android.music.domain.repository

import com.apple.android.music.domain.model.AppUpdateInfo
import com.apple.android.music.domain.model.UpdateDownloadProgress
import kotlinx.coroutines.flow.Flow
import java.io.File

interface UpdateRepository {
    fun getCurrentVersionName(): String
    suspend fun checkForUpdate(): Result<AppUpdateInfo>
    fun downloadUpdate(downloadUrl: String): Flow<UpdateDownloadProgress>
    fun getDownloadedApkFile(): File?
    fun deleteDownloadedApk()
}
