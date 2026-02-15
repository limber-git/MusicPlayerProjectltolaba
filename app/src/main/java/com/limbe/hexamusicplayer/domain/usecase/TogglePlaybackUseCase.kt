package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort

class TogglePlaybackUseCase(
    private val audioPlayerPort: AudioPlayerPort
) {
    operator fun invoke() = audioPlayerPort.togglePlayPause()
}
