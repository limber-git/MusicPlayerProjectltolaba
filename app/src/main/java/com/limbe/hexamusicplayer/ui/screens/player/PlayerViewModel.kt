package com.limbe.hexamusicplayer.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limbe.hexamusicplayer.domain.model.RepeatMode
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val toggleFavoriteTrackUseCase: ToggleFavoriteTrackUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var lastAttachedSessionId: Int? = null

    init {
        observePlayerState()
        observeAudioEffectsState()
        observeUserPreferences()
        loadPersistedSettings()
    }

    private fun loadPersistedSettings() {
        viewModelScope.launch {
            val prefs = observeUserPreferencesUseCase().first()
            setPlaybackSpeedUseCase(prefs.playbackSpeed)
            setPlaybackPitchUseCase(prefs.playbackPitch)
        }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            observeUserPreferencesUseCase().collect { prefs ->
                _uiState.update { state ->
                    val currentTrackId = state.currentTrack?.id
                    state.copy(
                        isFavorite = currentTrackId != null && prefs.favoriteTrackIds.contains(currentTrackId)
                    )
                }
            }
        }
    }

    fun playTrack(track: Track, queue: List<Track> = emptyList()) {
        playTrackUseCase(track, queue)
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

    fun setSpeed(speed: Float) {
        setPlaybackSpeedUseCase(speed)
        viewModelScope.launch { savePlaybackSpeedUseCase(speed) }
    }

    fun setPitch(pitch: Float) {
        setPlaybackPitchUseCase(pitch)
        viewModelScope.launch { savePlaybackPitchUseCase(pitch) }
    }

    fun setEqBandLevel(index: Int, level: Int) {
        setEqBandLevelUseCase(index, level)
        viewModelScope.launch { saveEqBandLevelUseCase(index, level) }
    }

    fun setBassStrength(strength: Int) {
        setBassStrengthUseCase(strength)
        viewModelScope.launch { saveBassStrengthUseCase(strength) }
    }

    fun setVirtualizerStrength(strength: Int) {
        setVirtualizerStrengthUseCase(strength)
        viewModelScope.launch { saveVirtualizerStrengthUseCase(strength) }
    }

    fun setLoudnessGain(gainMb: Int) {
        setLoudnessGainUseCase(gainMb)
        viewModelScope.launch { saveLoudnessGainUseCase(gainMb) }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            observePlayerStateUseCase().collect { playerState ->
                val prefs = observeUserPreferencesUseCase().first()
                _uiState.update {
                    it.copy(
                        currentTrack = playerState.currentTrack,
                        isPlaying = playerState.isPlaying,
                        currentPositionMs = playerState.positionMs,
                        durationMs = playerState.durationMs,
                        speed = playerState.speed,
                        pitch = playerState.pitch,
                        shuffleModeEnabled = playerState.shuffleModeEnabled,
                        repeatMode = playerState.repeatMode,
                        isFavorite = playerState.currentTrack?.id?.let { id -> prefs.favoriteTrackIds.contains(id) } ?: false
                    )
                }

                val audioSessionId = playerState.audioSessionId
                if (audioSessionId != null && audioSessionId > 0 && audioSessionId != lastAttachedSessionId) {
                    attachAudioEffectsUseCase(audioSessionId)
                    lastAttachedSessionId = audioSessionId
                    applyPersistedEffects()
                }
            }
        }
    }

    private suspend fun applyPersistedEffects() {
        val prefs = observeUserPreferencesUseCase().first()
        setBassStrengthUseCase(prefs.bassStrength)
        setVirtualizerStrengthUseCase(prefs.virtualizerStrength)
        setLoudnessGainUseCase(prefs.loudnessGainMb)
        prefs.eqBandLevels.forEach { (index, level) ->
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
                        loudnessGainMb = effectsState.loudnessGainMb
                    )
                }
            }
        }
    }
}
