package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.AudioEffectsPort
import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort

class ReleaseAudioEnginesUseCase(
    private val audioPlayerPort: AudioPlayerPort,
    private val audioEffectsPort: AudioEffectsPort
) {
    operator fun invoke() {
        audioEffectsPort.release()
        audioPlayerPort.release()
    }
}
