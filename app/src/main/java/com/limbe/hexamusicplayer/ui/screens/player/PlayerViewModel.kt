package com.limbe.hexamusicplayer.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limbe.hexamusicplayer.domain.model.AppLanguage
import com.limbe.hexamusicplayer.domain.model.DarkModeMode
import com.limbe.hexamusicplayer.domain.model.RepeatMode
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.model.UserPreferences
import com.limbe.hexamusicplayer.domain.usecase.AttachAudioEffectsUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveAudioEffectsStateUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObservePlayerStateUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveUserPreferencesUseCase
import com.limbe.hexamusicplayer.domain.usecase.PlayTrackUseCase
import com.limbe.hexamusicplayer.domain.usecase.RecordRecentTrackUseCase
import com.limbe.hexamusicplayer.domain.usecase.ReleaseAudioEffectsOnlyUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveBassStrengthUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveEqBandLevelUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveLoudnessGainUseCase
import com.limbe.hexamusicplayer.domain.usecase.SavePlaybackPitchUseCase
import com.limbe.hexamusicplayer.domain.usecase.SavePlaybackSpeedUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveVirtualizerStrengthUseCase
import com.limbe.hexamusicplayer.domain.usecase.SeekToUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetAudioEffectsEnabledUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetAppLanguageUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetBassStrengthUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetDarkModeUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetEqBandLevelUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetLoudnessGainUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetPlaybackPitchUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetPlaybackSpeedUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetRepeatModeUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetVirtualizerStrengthUseCase
import com.limbe.hexamusicplayer.domain.usecase.SkipToNextUseCase
import com.limbe.hexamusicplayer.domain.usecase.SkipToPreviousUseCase
import com.limbe.hexamusicplayer.domain.usecase.ToggleFavoriteTrackUseCase
import com.limbe.hexamusicplayer.domain.usecase.TogglePlaybackUseCase
import com.limbe.hexamusicplayer.domain.usecase.ToggleShuffleUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetManualLibraryFolderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playTrackUseCase: PlayTrackUseCase,
    private val togglePlaybackUseCase: TogglePlaybackUseCase,
    private val seekToUseCase: SeekToUseCase,
    private val setPlaybackSpeedUseCase: SetPlaybackSpeedUseCase,
    private val setPlaybackPitchUseCase: SetPlaybackPitchUseCase,
    private val observePlayerStateUseCase: ObservePlayerStateUseCase,
    private val attachAudioEffectsUseCase: AttachAudioEffectsUseCase,
    private val releaseAudioEffectsOnlyUseCase: ReleaseAudioEffectsOnlyUseCase,
    private val setEqBandLevelUseCase: SetEqBandLevelUseCase,
    private val setBassStrengthUseCase: SetBassStrengthUseCase,
    private val setVirtualizerStrengthUseCase: SetVirtualizerStrengthUseCase,
    private val setLoudnessGainUseCase: SetLoudnessGainUseCase,
    private val observeAudioEffectsStateUseCase: ObserveAudioEffectsStateUseCase,
    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val savePlaybackSpeedUseCase: SavePlaybackSpeedUseCase,
    private val savePlaybackPitchUseCase: SavePlaybackPitchUseCase,
    private val saveBassStrengthUseCase: SaveBassStrengthUseCase,
    private val saveVirtualizerStrengthUseCase: SaveVirtualizerStrengthUseCase,
    private val saveLoudnessGainUseCase: SaveLoudnessGainUseCase,
    private val saveEqBandLevelUseCase: SaveEqBandLevelUseCase,
    private val skipToNextUseCase: SkipToNextUseCase,
    private val skipToPreviousUseCase: SkipToPreviousUseCase,
    private val toggleShuffleUseCase: ToggleShuffleUseCase,
    private val setRepeatModeUseCase: SetRepeatModeUseCase,
    private val toggleFavoriteTrackUseCase: ToggleFavoriteTrackUseCase,
    private val recordRecentTrackUseCase: RecordRecentTrackUseCase,
    private val setDarkModeUseCase: SetDarkModeUseCase,
    private val setAudioEffectsEnabledUseCase: SetAudioEffectsEnabledUseCase,
    private val setAppLanguageUseCase: SetAppLanguageUseCase,
    private val setManualLibraryFolderUseCase: SetManualLibraryFolderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var lastAttachedSessionId: Int? = null
    private var lastKnownPlayerSessionId: Int? = null
    private var lastRecordedRecentTrackId: Long? = null
    private var currentPreferences = UserPreferences()
    private var hasAppliedInitialPlaybackSettings = false

    init {
        observeUserPreferences()
        observePlayerState()
        observeAudioEffectsState()
    }

    fun playTrack(track: Track, queue: List<Track> = emptyList()) {
        playTrackUseCase(track, queue)
        recordRecentTrack(track.id)
    }

    fun togglePlayback() {
        togglePlaybackUseCase()
    }

    fun seekTo(positionMs: Long) {
        seekToUseCase(positionMs)
    }

    fun skipToNext() {
        skipToNextUseCase()
    }

    fun skipToPrevious() {
        skipToPreviousUseCase()
    }

    fun toggleShuffle() {
        toggleShuffleUseCase(!_uiState.value.shuffleModeEnabled)
    }

    fun toggleRepeat() {
        val nextMode = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        setRepeatModeUseCase(nextMode)
    }

    fun toggleFavorite() {
        val trackId = _uiState.value.currentTrack?.id ?: return
        viewModelScope.launch {
            toggleFavoriteTrackUseCase(trackId)
        }
    }

    fun setDarkModeMode(mode: DarkModeMode) {
        if (_uiState.value.darkModeMode == mode) return
        viewModelScope.launch {
            setDarkModeUseCase(mode)
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        if (_uiState.value.appLanguage == language) return
        viewModelScope.launch {
            setAppLanguageUseCase(language)
        }
    }

    fun setManualLibraryFolder(uri: String?, label: String?) {
        viewModelScope.launch {
            setManualLibraryFolderUseCase(uri, label)
        }
    }

    fun setAudioEffectsEnabled(enabled: Boolean) {
        if (_uiState.value.audioEffectsEnabled == enabled) return

        viewModelScope.launch {
            setAudioEffectsEnabledUseCase(enabled)
        }

        if (!enabled) {
            lastAttachedSessionId = null
            releaseAudioEffectsOnlyUseCase()
        } else {
            val playerSessionId = lastKnownPlayerSessionId
            if (playerSessionId != null && playerSessionId > 0) {
                attachAudioEffectsIfAllowed(playerSessionId)
            }
        }
    }

    fun setSpeed(speed: Float) {
        setPlaybackSpeedUseCase(speed)
        viewModelScope.launch { savePlaybackSpeedUseCase(speed) }
    }

    fun setPitch(pitch: Float) {
        setPlaybackPitchUseCase(pitch)
        viewModelScope.launch { savePlaybackPitchUseCase(pitch) }
    }

    fun setEqBandLevel(index: Int, level: Int) {
        if (!currentPreferences.audioEffectsEnabled) return
        setEqBandLevelUseCase(index, level)
        viewModelScope.launch { saveEqBandLevelUseCase(index, level) }
    }

    fun applyEqPreset(levels: List<Int>) {
        if (!currentPreferences.audioEffectsEnabled) return

        val bands = _uiState.value.eqBands
        bands.forEachIndexed { position, band ->
            val level = levels.getOrNull(position) ?: 0
            setEqBandLevelUseCase(band.index, level)
            viewModelScope.launch { saveEqBandLevelUseCase(band.index, level) }
        }
    }

    fun resetAudioStudio() {
        setSpeed(DEFAULT_SPEED)
        setPitch(DEFAULT_PITCH)
        if (currentPreferences.audioEffectsEnabled) {
            _uiState.value.eqBands.forEach { band ->
                setEqBandLevel(band.index, 0)
            }
            setBassStrength(0)
            setVirtualizerStrength(0)
            setLoudnessGain(0)
        }
    }

    fun setBassStrength(strength: Int) {
        if (!currentPreferences.audioEffectsEnabled) return
        setBassStrengthUseCase(strength)
        viewModelScope.launch { saveBassStrengthUseCase(strength) }
    }

    fun setVirtualizerStrength(strength: Int) {
        if (!currentPreferences.audioEffectsEnabled) return
        setVirtualizerStrengthUseCase(strength)
        viewModelScope.launch { saveVirtualizerStrengthUseCase(strength) }
    }

    fun setLoudnessGain(gainMb: Int) {
        if (!currentPreferences.audioEffectsEnabled) return
        setLoudnessGainUseCase(gainMb)
        viewModelScope.launch { saveLoudnessGainUseCase(gainMb) }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            observeUserPreferencesUseCase().collect { prefs ->
                val previousEffectsEnabled = currentPreferences.audioEffectsEnabled
                currentPreferences = prefs

                if (!hasAppliedInitialPlaybackSettings) {
                    setPlaybackSpeedUseCase(prefs.playbackSpeed)
                    setPlaybackPitchUseCase(prefs.playbackPitch)
                    hasAppliedInitialPlaybackSettings = true
                }

                if (!prefs.audioEffectsEnabled) {
                    lastAttachedSessionId = null
                    releaseAudioEffectsOnlyUseCase()
                } else if (!previousEffectsEnabled && prefs.audioEffectsEnabled) {
                    lastKnownPlayerSessionId?.takeIf { it > 0 }?.let { attachAudioEffectsIfAllowed(it) }
                }

                _uiState.update { state ->
                    val currentTrackId = state.currentTrack?.id
                    state.copy(
                        isFavorite = currentTrackId != null && prefs.favoriteTrackIds.contains(currentTrackId),
                        favoriteCount = prefs.favoriteTrackIds.size,
                        recentTrackIds = prefs.recentTrackIds,
                        darkModeMode = prefs.darkModeMode,
                        audioEffectsEnabled = prefs.audioEffectsEnabled,
                        appLanguage = prefs.appLanguage,
                        manualLibraryFolderUri = prefs.manualLibraryFolderUri,
                        manualLibraryFolderLabel = prefs.manualLibraryFolderLabel
                    )
                }
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            observePlayerStateUseCase().collect { playerState ->
                _uiState.update {
                    it.copy(
                        currentTrack = playerState.currentTrack,
                        queue = playerState.queue,
                        isPlaying = playerState.isPlaying,
                        currentPositionMs = playerState.positionMs,
                        durationMs = playerState.durationMs,
                        speed = playerState.speed,
                        pitch = playerState.pitch,
                        shuffleModeEnabled = playerState.shuffleModeEnabled,
                        repeatMode = playerState.repeatMode,
                        playerErrorMessage = playerState.errorMessage,
                        isFavorite = playerState.currentTrack?.id?.let { id ->
                            currentPreferences.favoriteTrackIds.contains(id)
                        } ?: false
                    )
                }

                playerState.currentTrack?.id?.let(::recordRecentTrack)

                val audioSessionId = playerState.audioSessionId
                if (audioSessionId != null && audioSessionId > 0) {
                    lastKnownPlayerSessionId = audioSessionId
                    attachAudioEffectsIfAllowed(audioSessionId)
                }
            }
        }
    }

    private fun attachAudioEffectsIfAllowed(audioSessionId: Int) {
        if (!currentPreferences.audioEffectsEnabled) return
        if (audioSessionId == lastAttachedSessionId) return

        attachAudioEffectsUseCase(audioSessionId)
        lastAttachedSessionId = audioSessionId
        applyPersistedEffects()
    }

    private fun applyPersistedEffects() {
        if (!currentPreferences.audioEffectsEnabled) return

        setBassStrengthUseCase(currentPreferences.bassStrength)
        setVirtualizerStrengthUseCase(currentPreferences.virtualizerStrength)
        setLoudnessGainUseCase(currentPreferences.loudnessGainMb)
        currentPreferences.eqBandLevels.forEach { (index, level) ->
            setEqBandLevelUseCase(index, level)
        }
    }

    private fun observeAudioEffectsState() {
        viewModelScope.launch {
            observeAudioEffectsStateUseCase().collect { effectsState ->
                _uiState.update {
                    it.copy(
                        attachedSessionId = effectsState.attachedSessionId,
                        eqBands = effectsState.bands,
                        bassStrength = effectsState.bassStrength,
                        virtualizerStrength = effectsState.virtualizerStrength,
                        loudnessGainMb = effectsState.loudnessGainMb,
                        effectsAvailable = effectsState.effectsAvailable
                    )
                }
            }
        }
    }

    private fun recordRecentTrack(trackId: Long) {
        if (lastRecordedRecentTrackId == trackId) return
        lastRecordedRecentTrackId = trackId
        viewModelScope.launch {
            recordRecentTrackUseCase(trackId)
        }
    }

    private companion object {
        const val DEFAULT_SPEED = 1.0f
        const val DEFAULT_PITCH = 1.0f
    }
}
