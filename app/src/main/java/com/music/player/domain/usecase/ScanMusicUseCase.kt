package com.music.player.domain.usecase

import com.music.player.domain.repository.MusicRepository
import javax.inject.Inject

class ScanMusicUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke() {
        musicRepository.refreshMusic()
    }
}
