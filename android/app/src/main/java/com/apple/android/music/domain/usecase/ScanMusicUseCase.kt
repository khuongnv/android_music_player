package com.apple.android.music.domain.usecase

import com.apple.android.music.domain.repository.MusicRepository
import javax.inject.Inject

class ScanMusicUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke() {
        musicRepository.refreshMusic()
    }
}
