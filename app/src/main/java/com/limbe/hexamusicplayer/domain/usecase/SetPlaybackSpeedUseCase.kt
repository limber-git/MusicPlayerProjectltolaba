package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort

class SetPlaybackSpeedUseCase(
    private val audioPlayerPort: AudioPlayerPort
) {
    operator fun invoke(speed: Float) {
        audioPlayerPort.setSpeed(speed.coerceIn(0.5f, 2.0f))
    }
}
