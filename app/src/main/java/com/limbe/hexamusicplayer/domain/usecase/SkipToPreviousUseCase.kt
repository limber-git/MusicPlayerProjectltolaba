package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort

class SkipToPreviousUseCase(private val audioPlayerPort: AudioPlayerPort) {
    operator fun invoke() = audioPlayerPort.skipToPrevious()
}
