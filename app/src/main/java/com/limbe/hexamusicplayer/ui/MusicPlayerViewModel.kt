package com.limbe.hexamusicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.usecase.AttachAudioEffectsUseCase
import com.limbe.hexamusicplayer.domain.usecase.GetLocalTracksUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveAudioEffectsStateUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObservePlayerStateUseCase
import com.limbe.hexamusicplayer.domain.usecase.PlayTrackUseCase
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MusicPlayerViewModel(
    private val getLocalTracksUseCase: GetLocalTracksUseCase,
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
    private val observeAudioEffectsStateUseCase: ObserveAudioEffectsStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicPlayerUiState())
    val uiState: StateFlow<MusicPlayerUiState> = _uiState.asStateFlow()

    private var lastAttachedSessionId: Int? = null

    init {
        observePlayerState()
        observeAudioEffectsState()
    }

    fun refreshTracks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { getLocalTracksUseCase() }
                .onSuccess { tracks ->
                    _uiState.update {
                        it.copy(
                            tracks = tracks,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Error loading local music"
                        )
                    }
                }
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
    }

    fun setPitch(pitch: Float) {
        setPlaybackPitchUseCase(pitch)
    }

    fun setEqBandLevel(index: Int, level: Int) {
        setEqBandLevelUseCase(index, level)
    }

    fun setBassStrength(strength: Int) {
        setBassStrengthUseCase(strength)
    }

    fun setVirtualizerStrength(strength: Int) {
        setVirtualizerStrengthUseCase(strength)
    }

    fun setLoudnessGain(gainMb: Int) {
        setLoudnessGainUseCase(gainMb)
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
                }
            }
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

    override fun onCleared() = super.onCleared()
}
