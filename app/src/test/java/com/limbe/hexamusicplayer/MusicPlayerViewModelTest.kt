package com.limbe.hexamusicplayer

import com.limbe.hexamusicplayer.domain.model.AudioEffectsState
import com.limbe.hexamusicplayer.domain.model.PlayerState
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.AudioEffectsPort
import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort
import com.limbe.hexamusicplayer.domain.port.LocalMusicRepository
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
import com.limbe.hexamusicplayer.ui.MusicPlayerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MusicPlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `refresh tracks loads local songs on success`() = runTest {
        val fakeRepository = FakeLocalMusicRepository(
            tracks = listOf(
                Track(
                    id = 1L,
                    title = "Track One",
                    artist = "Artist A",
                    durationMs = 120_000L,
                    contentUri = "content://song/1"
                )
            )
        )

        val fakePlayer = FakeViewModelPlayerPort()
        val fakeEffects = FakeAudioEffectsPort()
        val viewModel = createViewModel(fakeRepository, fakePlayer, fakeEffects)

        viewModel.refreshTracks()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.tracks.size)
        assertEquals("Track One", viewModel.uiState.value.tracks.first().title)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `refresh tracks exposes error on failure`() = runTest {
        val fakeRepository = FakeLocalMusicRepository(
            error = IllegalStateException("Storage unavailable")
        )

        val fakePlayer = FakeViewModelPlayerPort()
        val fakeEffects = FakeAudioEffectsPort()
        val viewModel = createViewModel(fakeRepository, fakePlayer, fakeEffects)

        viewModel.refreshTracks()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.tracks.isEmpty())
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("Storage unavailable", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `player session attachment propagates to effects`() = runTest {
        val fakeRepository = FakeLocalMusicRepository()
        val fakePlayer = FakeViewModelPlayerPort()
        val fakeEffects = FakeAudioEffectsPort()
        val viewModel = createViewModel(fakeRepository, fakePlayer, fakeEffects)

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
        repository: LocalMusicRepository,
        player: FakeViewModelPlayerPort,
        effects: FakeAudioEffectsPort
    ): MusicPlayerViewModel {
        return MusicPlayerViewModel(
            getLocalTracksUseCase = GetLocalTracksUseCase(repository),
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

private class FakeLocalMusicRepository(
    private val tracks: List<Track> = emptyList(),
    private val error: Throwable? = null
) : LocalMusicRepository {
    override suspend fun listLocalTracks(): List<Track> {
        error?.let { throw it }
        return tracks
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
