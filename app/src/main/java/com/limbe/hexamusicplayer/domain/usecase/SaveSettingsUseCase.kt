package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.UserPreferencesPort

class SavePlaybackSpeedUseCase(private val port: UserPreferencesPort) {
    suspend operator fun invoke(speed: Float) = port.setPlaybackSpeed(speed)
}

class SavePlaybackPitchUseCase(private val port: UserPreferencesPort) {
    suspend operator fun invoke(pitch: Float) = port.setPlaybackPitch(pitch)
}

class SaveBassStrengthUseCase(private val port: UserPreferencesPort) {
    suspend operator fun invoke(strength: Int) = port.setBassStrength(strength)
}

class SaveVirtualizerStrengthUseCase(private val port: UserPreferencesPort) {
    suspend operator fun invoke(strength: Int) = port.setVirtualizerStrength(strength)
}

class SaveLoudnessGainUseCase(private val port: UserPreferencesPort) {
    suspend operator fun invoke(gainMb: Int) = port.setLoudnessGainMb(gainMb)
}

class SaveEqBandLevelUseCase(private val port: UserPreferencesPort) {
    suspend operator fun invoke(index: Int, level: Int) = port.setEqBandLevel(index, level)
}

class RecordRecentTrackUseCase(private val port: UserPreferencesPort) {
    suspend operator fun invoke(trackId: Long) = port.recordRecentTrack(trackId)
}

class SetAudioEffectsEnabledUseCase(private val port: UserPreferencesPort) {
    suspend operator fun invoke(enabled: Boolean) = port.setAudioEffectsEnabled(enabled)
}
