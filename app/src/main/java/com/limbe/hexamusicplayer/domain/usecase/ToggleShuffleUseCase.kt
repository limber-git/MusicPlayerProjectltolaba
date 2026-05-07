package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort

class ToggleShuffleUseCase(private val audioPlayerPort: AudioPlayerPort) {
    operator fun invoke(enabled: Boolean) = audioPlayerPort.setShuffleMode(enabled)
}
