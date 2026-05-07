package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.model.RepeatMode
import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort

class SetRepeatModeUseCase(private val audioPlayerPort: AudioPlayerPort) {
    operator fun invoke(mode: RepeatMode) = audioPlayerPort.setRepeatMode(mode)
}
