package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort

class PlayTrackUseCase(
    private val audioPlayerPort: AudioPlayerPort
) {
    operator fun invoke(track: Track) = audioPlayerPort.play(track)
}
