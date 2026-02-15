package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.model.PlayerState
import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort
import kotlinx.coroutines.flow.StateFlow

class ObservePlayerStateUseCase(
    private val audioPlayerPort: AudioPlayerPort
) {
    operator fun invoke(): StateFlow<PlayerState> = audioPlayerPort.state
}
