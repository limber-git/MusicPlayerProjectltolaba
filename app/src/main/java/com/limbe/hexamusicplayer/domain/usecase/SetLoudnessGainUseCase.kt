package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.AudioEffectsPort

class SetLoudnessGainUseCase(
    private val audioEffectsPort: AudioEffectsPort
) {
    operator fun invoke(gainMb: Int) = audioEffectsPort.setLoudnessGainMb(gainMb)
}
