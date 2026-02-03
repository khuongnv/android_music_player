package com.apple.android.music.domain.usecase

import com.apple.android.music.domain.model.Song
import com.apple.android.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return musicRepository.getSongsFlow()
    }
}
