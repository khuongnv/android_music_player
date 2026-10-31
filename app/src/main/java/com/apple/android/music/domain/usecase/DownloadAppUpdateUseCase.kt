package com.apple.android.music.domain.usecase

import com.apple.android.music.domain.model.UpdateDownloadProgress
import com.apple.android.music.domain.repository.UpdateRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class DownloadAppUpdateUseCase @Inject constructor(
    private val updateRepository: UpdateRepository
) {
    operator fun invoke(downloadUrl: String): Flow<UpdateDownloadProgress> {
        return updateRepository.downloadUpdate(downloadUrl)
    }

    fun getDownloadedApkFile(): File? {
        return updateRepository.getDownloadedApkFile()
    }

    fun deleteDownloadedApk() {
        updateRepository.deleteDownloadedApk()
    }
}
