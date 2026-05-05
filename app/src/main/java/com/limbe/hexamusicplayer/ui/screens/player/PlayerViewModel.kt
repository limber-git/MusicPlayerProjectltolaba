package com.limbe.hexamusicplayer.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.usecase.AttachAudioEffectsUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveAudioEffectsStateUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObservePlayerStateUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveUserPreferencesUseCase
import com.limbe.hexamusicplayer.domain.usecase.PlayTrackUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveBassStrengthUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveEqBandLevelUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveLoudnessGainUseCase
import com.limbe.hexamusicplayer.domain.usecase.SavePlaybackPitchUseCase
import com.limbe.hexamusicplayer.domain.usecase.SavePlaybackSpeedUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveVirtualizerStrengthUseCase
import com.limbe.hexamusicplayer.domain.usecase.SeekToUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetBassStrengthUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetEqBandLevelUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetLoudnessGainUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetPlaybackPitchUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetPlaybackSpeedUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetVirtualizerStrengthUseCase
import com.limbe.hexamusicplayer.domain.usecase.TogglePlaybackUseCase
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
    private val saveEqBandLevelUseCase: SaveEqBandLevelUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var lastAttachedSessionId: Int? = null
    private var isInitialLoad = true

    init {
        observePlayerState()
        observeAudioEffectsState()
        loadPersistedSettings()
    }

    private fun loadPersistedSettings() {
        viewModelScope.launch {
            val prefs = observeUserPreferencesUseCase().first()
            setPlaybackSpeedUseCase(prefs.playbackSpeed)
            setPlaybackPitchUseCase(prefs.playbackPitch)
            // EQ and effects will be applied when session is attached
            isInitialLoad = false
        }
    }

    fun playTrack(track: Track) {
        playTrackUseCase(track)
    }

    fun togglePlayback() {
        togglePlaybackUseCase()
    }

    fun seekTo(positionMs: Long) {
        seekToUseCase(positionMs)
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
                _uiState.update {
                    it.copy(
                        currentTrack = playerState.currentTrack,
                        isPlaying = playerState.isPlaying,
                        currentPositionMs = playerState.positionMs,
                        durationMs = playerState.durationMs,
                        speed = playerState.speed,
                        pitch = playerState.pitch
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
