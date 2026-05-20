package com.limbe.hexamusicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.limbe.hexamusicplayer.app.AppContainer
import com.limbe.hexamusicplayer.ui.screens.library.LibraryViewModel
import com.limbe.hexamusicplayer.ui.screens.player.PlayerViewModel

class MusicPlayerViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LibraryViewModel::class.java) -> {
                LibraryViewModel(
                    getLocalTracksUseCase = container.getLocalTracksUseCase,
                    observeUserPreferencesUseCase = container.observeUserPreferencesUseCase
                ) as T
            }
            modelClass.isAssignableFrom(PlayerViewModel::class.java) -> {
                PlayerViewModel(
                    playTrackUseCase = container.playTrackUseCase,
                    togglePlaybackUseCase = container.togglePlaybackUseCase,
                    seekToUseCase = container.seekToUseCase,
                    setPlaybackSpeedUseCase = container.setPlaybackSpeedUseCase,
                    setPlaybackPitchUseCase = container.setPlaybackPitchUseCase,
                    observePlayerStateUseCase = container.observePlayerStateUseCase,
                    attachAudioEffectsUseCase = container.attachAudioEffectsUseCase,
                    releaseAudioEffectsOnlyUseCase = container.releaseAudioEffectsOnlyUseCase,
                    setEqBandLevelUseCase = container.setEqBandLevelUseCase,
                    setBassStrengthUseCase = container.setBassStrengthUseCase,
                    setVirtualizerStrengthUseCase = container.setVirtualizerStrengthUseCase,
                    setLoudnessGainUseCase = container.setLoudnessGainUseCase,
                    observeAudioEffectsStateUseCase = container.observeAudioEffectsStateUseCase,
                    observeUserPreferencesUseCase = container.observeUserPreferencesUseCase,
                    savePlaybackSpeedUseCase = container.savePlaybackSpeedUseCase,
                    savePlaybackPitchUseCase = container.savePlaybackPitchUseCase,
                    saveBassStrengthUseCase = container.saveBassStrengthUseCase,
                    saveVirtualizerStrengthUseCase = container.saveVirtualizerStrengthUseCase,
                    saveLoudnessGainUseCase = container.saveLoudnessGainUseCase,
                    saveEqBandLevelUseCase = container.saveEqBandLevelUseCase,
                    skipToNextUseCase = container.skipToNextUseCase,
                    skipToPreviousUseCase = container.skipToPreviousUseCase,
                    toggleShuffleUseCase = container.toggleShuffleUseCase,
                    setRepeatModeUseCase = container.setRepeatModeUseCase,
                    toggleFavoriteTrackUseCase = container.toggleFavoriteTrackUseCase,
                    recordRecentTrackUseCase = container.recordRecentTrackUseCase,
                    setDarkModeUseCase = container.setDarkModeUseCase,
                    setAudioEffectsEnabledUseCase = container.setAudioEffectsEnabledUseCase,
                    setAppLanguageUseCase = container.setAppLanguageUseCase,
                    setManualLibraryFolderUseCase = container.setManualLibraryFolderUseCase
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
