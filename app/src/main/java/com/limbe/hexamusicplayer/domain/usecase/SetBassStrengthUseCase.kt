package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.AudioEffectsPort

class SetBassStrengthUseCase(
    private val audioEffectsPort: AudioEffectsPort
) {
    operator fun invoke(strength: Int) = audioEffectsPort.setBassStrength(strength)
}
