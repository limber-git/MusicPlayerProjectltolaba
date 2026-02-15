package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.AudioEffectsPort

class SetVirtualizerStrengthUseCase(
    private val audioEffectsPort: AudioEffectsPort
) {
    operator fun invoke(strength: Int) = audioEffectsPort.setVirtualizerStrength(strength)
}
