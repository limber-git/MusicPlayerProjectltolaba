package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.AudioEffectsPort

class SetEqBandLevelUseCase(
    private val audioEffectsPort: AudioEffectsPort
) {
    operator fun invoke(index: Int, level: Int) = audioEffectsPort.setBandLevel(index, level)
}
