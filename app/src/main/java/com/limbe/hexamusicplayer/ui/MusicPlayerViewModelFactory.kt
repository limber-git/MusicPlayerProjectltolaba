package com.limbe.hexamusicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.limbe.hexamusicplayer.app.AppContainer

class MusicPlayerViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicPlayerViewModel::class.java)) {
            return MusicPlayerViewModel(
                getLocalTracksUseCase = container.getLocalTracksUseCase,
                playTrackUseCase = container.playTrackUseCase,
                togglePlaybackUseCase = container.togglePlaybackUseCase,
                seekToUseCase = container.seekToUseCase,
                setPlaybackSpeedUseCase = container.setPlaybackSpeedUseCase,
                setPlaybackPitchUseCase = container.setPlaybackPitchUseCase,
                observePlayerStateUseCase = container.observePlayerStateUseCase,
                attachAudioEffectsUseCase = container.attachAudioEffectsUseCase,
                setEqBandLevelUseCase = container.setEqBandLevelUseCase,
                setBassStrengthUseCase = container.setBassStrengthUseCase,
                setVirtualizerStrengthUseCase = container.setVirtualizerStrengthUseCase,
                setLoudnessGainUseCase = container.setLoudnessGainUseCase,
                observeAudioEffectsStateUseCase = container.observeAudioEffectsStateUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
