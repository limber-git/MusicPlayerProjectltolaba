package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.model.AudioEffectsState
import com.limbe.hexamusicplayer.domain.port.AudioEffectsPort
import kotlinx.coroutines.flow.StateFlow

class ObserveAudioEffectsStateUseCase(
    private val audioEffectsPort: AudioEffectsPort
) {
    operator fun invoke(): StateFlow<AudioEffectsState> = audioEffectsPort.state
}
