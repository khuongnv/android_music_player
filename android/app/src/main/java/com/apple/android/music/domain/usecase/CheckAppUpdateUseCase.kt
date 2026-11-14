package com.apple.android.music.domain.usecase

import com.apple.android.music.domain.model.AppUpdateInfo
import com.apple.android.music.domain.repository.UpdateRepository
import javax.inject.Inject

class CheckAppUpdateUseCase @Inject constructor(
    private val updateRepository: UpdateRepository
) {
    suspend operator fun invoke(): Result<AppUpdateInfo> {
        return updateRepository.checkForUpdate()
    }

    fun getCurrentVersion(): String {
        return updateRepository.getCurrentVersionName()
    }
}
