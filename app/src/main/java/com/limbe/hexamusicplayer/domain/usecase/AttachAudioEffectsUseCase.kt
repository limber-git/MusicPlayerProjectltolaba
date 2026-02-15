package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.AudioEffectsPort

class AttachAudioEffectsUseCase(
    private val audioEffectsPort: AudioEffectsPort
) {
    operator fun invoke(sessionId: Int) = audioEffectsPort.attachToSession(sessionId)
}
