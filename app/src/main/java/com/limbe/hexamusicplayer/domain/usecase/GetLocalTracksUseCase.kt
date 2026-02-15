package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.LocalMusicRepository

class GetLocalTracksUseCase(
    private val repository: LocalMusicRepository
) {
    suspend operator fun invoke(): List<Track> = repository.listLocalTracks()
}
