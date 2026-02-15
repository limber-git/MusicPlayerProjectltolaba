package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort

class SetPlaybackPitchUseCase(
    private val audioPlayerPort: AudioPlayerPort
) {
    operator fun invoke(pitch: Float) {
        audioPlayerPort.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }
}
