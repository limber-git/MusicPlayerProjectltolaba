package com.limbe.hexamusicplayer

import com.limbe.hexamusicplayer.domain.model.AudioEffectsState
import com.limbe.hexamusicplayer.domain.model.PlayerState
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.AudioEffectsPort
import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort
import com.limbe.hexamusicplayer.domain.usecase.AttachAudioEffectsUseCase
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
import com.limbe.hexamusicplayer.ui.screens.player.PlayerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `player session attachment propagates to effects`() = runTest {
        val fakePlayer = FakeViewModelPlayerPort()
        val fakeEffects = FakeAudioEffectsPort()
        val viewModel = createViewModel(fakePlayer, fakeEffects)

        fakePlayer.emit(
            PlayerState(
                currentTrack = Track(44L, "Nucleus", "DJ Hexa", 210_000L, "content://song/44"),
                isPlaying = true,
                audioSessionId = 321
            )
        )

        advanceUntilIdle()

        assertEquals(321, fakeEffects.lastAttachedSessionId)
        assertEquals(321, viewModel.uiState.value.attachedSessionId)
        assertNotNull(viewModel.uiState.value.currentTrack)
    }

    private fun createViewModel(
        player: FakeViewModelPlayerPort,
        effects: FakeAudioEffectsPort
    ): PlayerViewModel {
        return PlayerViewModel(
            playTrackUseCase = PlayTrackUseCase(player),
            togglePlaybackUseCase = TogglePlaybackUseCase(player),
            seekToUseCase = SeekToUseCase(player),
            setPlaybackSpeedUseCase = SetPlaybackSpeedUseCase(player),
            setPlaybackPitchUseCase = SetPlaybackPitchUseCase(player),
            observePlayerStateUseCase = ObservePlayerStateUseCase(player),
            attachAudioEffectsUseCase = AttachAudioEffectsUseCase(effects),
            setEqBandLevelUseCase = SetEqBandLevelUseCase(effects),
            setBassStrengthUseCase = SetBassStrengthUseCase(effects),
            setVirtualizerStrengthUseCase = SetVirtualizerStrengthUseCase(effects),
            setLoudnessGainUseCase = SetLoudnessGainUseCase(effects),
            observeAudioEffectsStateUseCase = ObserveAudioEffectsStateUseCase(effects)
        )
    }
}

private class FakeViewModelPlayerPort : AudioPlayerPort {
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state

    override fun play(track: Track) {
        _state.value = _state.value.copy(currentTrack = track, isPlaying = true)
    }

    override fun togglePlayPause() {
        _state.value = _state.value.copy(isPlaying = !_state.value.isPlaying)
    }

    override fun seekTo(positionMs: Long) {
        _state.value = _state.value.copy(positionMs = positionMs)
    }

    override fun setSpeed(speed: Float) {
        _state.value = _state.value.copy(speed = speed)
    }

    override fun setPitch(pitch: Float) {
        _state.value = _state.value.copy(pitch = pitch)
    }

    override fun release() = Unit

    fun emit(state: PlayerState) {
        _state.value = state
    }
}

private class FakeAudioEffectsPort : AudioEffectsPort {
    private val _state = MutableStateFlow(AudioEffectsState())
    override val state: StateFlow<AudioEffectsState> = _state

    var lastAttachedSessionId: Int? = null

    override fun attachToSession(sessionId: Int) {
        lastAttachedSessionId = sessionId
        _state.value = _state.value.copy(attachedSessionId = sessionId)
    }

    override fun setBandLevel(index: Int, level: Int) = Unit

    override fun setBassStrength(strength: Int) {
        _state.value = _state.value.copy(bassStrength = strength)
    }

    override fun setVirtualizerStrength(strength: Int) {
        _state.value = _state.value.copy(virtualizerStrength = strength)
    }

    override fun setLoudnessGainMb(gainMb: Int) {
        _state.value = _state.value.copy(loudnessGainMb = gainMb)
    }

    override fun release() {
        _state.value = AudioEffectsState()
    }
}
