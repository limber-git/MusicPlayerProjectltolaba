package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort

class SeekToUseCase(
    private val audioPlayerPort: AudioPlayerPort
) {
    operator fun invoke(positionMs: Long) {
        audioPlayerPort.seekTo(positionMs.coerceAtLeast(0L))
    }
}
