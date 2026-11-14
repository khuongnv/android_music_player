package com.apple.android.music.domain.usecase

import com.apple.android.music.domain.model.Artist
import com.apple.android.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArtistsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Artist>> {
        return musicRepository.getArtistsFlow()
    }
}
