package com.limbe.hexamusicplayer

import com.limbe.hexamusicplayer.domain.model.AudioEffectsState
import com.limbe.hexamusicplayer.domain.model.AppLanguage
import com.limbe.hexamusicplayer.domain.model.DarkModeMode
import com.limbe.hexamusicplayer.domain.model.PlayerState
import com.limbe.hexamusicplayer.domain.model.RepeatMode
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.model.UserPreferences
import com.limbe.hexamusicplayer.domain.port.AudioEffectsPort
import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort
import com.limbe.hexamusicplayer.domain.port.UserPreferencesPort
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
import com.limbe.hexamusicplayer.domain.usecase.SetManualLibraryFolderUseCase
import com.limbe.hexamusicplayer.domain.usecase.ToggleFavoriteTrackUseCase
import com.limbe.hexamusicplayer.domain.usecase.TogglePlaybackUseCase
import com.limbe.hexamusicplayer.domain.usecase.ToggleShuffleUseCase
import com.limbe.hexamusicplayer.ui.screens.player.PlayerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
        val fakePreferences = FakeUserPreferencesPort()
        val viewModel = createViewModel(fakePlayer, fakeEffects, fakePreferences)

        fakePlayer.emit(
            PlayerState(
                currentTrack = sampleTrack(44L),
                isPlaying = true,
                audioSessionId = 321
            )
        )

        advanceUntilIdle()

        assertEquals(321, fakeEffects.lastAttachedSessionId)
        assertEquals(321, viewModel.uiState.value.attachedSessionId)
        assertEquals(true, viewModel.uiState.value.effectsAvailable)
        assertNotNull(viewModel.uiState.value.currentTrack)
    }

    @Test
    fun `preferences update dark mode favorites and recents in ui state`() = runTest {
        val fakePlayer = FakeViewModelPlayerPort()
        val fakeEffects = FakeAudioEffectsPort()
        val fakePreferences = FakeUserPreferencesPort()
        val viewModel = createViewModel(fakePlayer, fakeEffects, fakePreferences)
        val currentTrack = sampleTrack(5L)

        fakePlayer.emit(PlayerState(currentTrack = currentTrack))
        fakePreferences.emit(
            UserPreferences(
                favoriteTrackIds = setOf(5L, 9L),
                recentTrackIds = listOf(5L, 2L, 1L),
                darkModeMode = DarkModeMode.DARK
            )
        )

        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isFavorite)
        assertEquals(2, viewModel.uiState.value.favoriteCount)
        assertEquals(3, viewModel.uiState.value.recentTrackIds.size)
        assertEquals(DarkModeMode.DARK, viewModel.uiState.value.darkModeMode)
    }

    @Test
    fun `disabling audio effects releases effects state`() = runTest {
        val fakePlayer = FakeViewModelPlayerPort()
        val fakeEffects = FakeAudioEffectsPort()
        val fakePreferences = FakeUserPreferencesPort()
        val viewModel = createViewModel(fakePlayer, fakeEffects, fakePreferences)

        fakePlayer.emit(PlayerState(currentTrack = sampleTrack(7L), audioSessionId = 222))
        advanceUntilIdle()

        viewModel.setAudioEffectsEnabled(false)
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.audioEffectsEnabled)
        assertEquals(false, viewModel.uiState.value.effectsAvailable)
        assertEquals(null, viewModel.uiState.value.attachedSessionId)
    }

    private fun createViewModel(
        player: FakeViewModelPlayerPort,
        effects: FakeAudioEffectsPort,
        preferences: FakeUserPreferencesPort
    ): PlayerViewModel {
        return PlayerViewModel(
            playTrackUseCase = PlayTrackUseCase(player),
            togglePlaybackUseCase = TogglePlaybackUseCase(player),
            seekToUseCase = SeekToUseCase(player),
            setPlaybackSpeedUseCase = SetPlaybackSpeedUseCase(player),
            setPlaybackPitchUseCase = SetPlaybackPitchUseCase(player),
            observePlayerStateUseCase = ObservePlayerStateUseCase(player),
            attachAudioEffectsUseCase = AttachAudioEffectsUseCase(effects),
            releaseAudioEffectsOnlyUseCase = ReleaseAudioEffectsOnlyUseCase(effects),
            setEqBandLevelUseCase = SetEqBandLevelUseCase(effects),
            setBassStrengthUseCase = SetBassStrengthUseCase(effects),
            setVirtualizerStrengthUseCase = SetVirtualizerStrengthUseCase(effects),
            setLoudnessGainUseCase = SetLoudnessGainUseCase(effects),
            observeAudioEffectsStateUseCase = ObserveAudioEffectsStateUseCase(effects),
            observeUserPreferencesUseCase = ObserveUserPreferencesUseCase(preferences),
            savePlaybackSpeedUseCase = SavePlaybackSpeedUseCase(preferences),
            savePlaybackPitchUseCase = SavePlaybackPitchUseCase(preferences),
            saveBassStrengthUseCase = SaveBassStrengthUseCase(preferences),
            saveVirtualizerStrengthUseCase = SaveVirtualizerStrengthUseCase(preferences),
            saveLoudnessGainUseCase = SaveLoudnessGainUseCase(preferences),
            saveEqBandLevelUseCase = SaveEqBandLevelUseCase(preferences),
            skipToNextUseCase = SkipToNextUseCase(player),
            skipToPreviousUseCase = SkipToPreviousUseCase(player),
            toggleShuffleUseCase = ToggleShuffleUseCase(player),
            setRepeatModeUseCase = SetRepeatModeUseCase(player),
            toggleFavoriteTrackUseCase = ToggleFavoriteTrackUseCase(preferences),
            recordRecentTrackUseCase = RecordRecentTrackUseCase(preferences),
            setDarkModeUseCase = SetDarkModeUseCase(preferences),
            setAudioEffectsEnabledUseCase = SetAudioEffectsEnabledUseCase(preferences),
            setAppLanguageUseCase = SetAppLanguageUseCase(preferences),
            setManualLibraryFolderUseCase = SetManualLibraryFolderUseCase(preferences)
        )
    }

    private fun sampleTrack(id: Long) = Track(
        id = id,
        title = "Track $id",
        artist = "DJ Hexa",
        album = "Core",
        albumId = 6L,
        durationMs = 210_000L,
        contentUri = "content://song/$id",
        artworkUri = "content://albumart/6"
    )
}

private class FakeViewModelPlayerPort : AudioPlayerPort {
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state

    override fun play(track: Track, queue: List<Track>) {
        _state.value = _state.value.copy(currentTrack = track, isPlaying = true, errorMessage = null)
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

    override fun skipToNext() = Unit
    override fun skipToPrevious() = Unit

    override fun setShuffleMode(enabled: Boolean) {
        _state.value = _state.value.copy(shuffleModeEnabled = enabled)
    }

    override fun setRepeatMode(mode: RepeatMode) {
        _state.value = _state.value.copy(repeatMode = mode)
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
        _state.value = _state.value.copy(
            attachedSessionId = sessionId,
            effectsAvailable = true
        )
    }

    override fun setBandLevel(index: Int, level: Int) = Unit

    override fun setBassStrength(strength: Int) {
        _state.value = _state.value.copy(bassStrength = strength, effectsAvailable = true)
    }

    override fun setVirtualizerStrength(strength: Int) {
        _state.value = _state.value.copy(virtualizerStrength = strength, effectsAvailable = true)
    }

    override fun setLoudnessGainMb(gainMb: Int) {
        _state.value = _state.value.copy(loudnessGainMb = gainMb, effectsAvailable = true)
    }

    override fun release() {
        _state.value = AudioEffectsState()
    }
}

private class FakeUserPreferencesPort : UserPreferencesPort {
    private val preferencesFlow = MutableStateFlow(UserPreferences())
    override val preferences: Flow<UserPreferences> = preferencesFlow

    override suspend fun setPlaybackSpeed(speed: Float) {
        preferencesFlow.value = preferencesFlow.value.copy(playbackSpeed = speed)
    }

    override suspend fun setPlaybackPitch(pitch: Float) {
        preferencesFlow.value = preferencesFlow.value.copy(playbackPitch = pitch)
    }

    override suspend fun setBassStrength(strength: Int) {
        preferencesFlow.value = preferencesFlow.value.copy(bassStrength = strength)
    }

    override suspend fun setVirtualizerStrength(strength: Int) {
        preferencesFlow.value = preferencesFlow.value.copy(virtualizerStrength = strength)
    }

    override suspend fun setLoudnessGainMb(gainMb: Int) {
        preferencesFlow.value = preferencesFlow.value.copy(loudnessGainMb = gainMb)
    }

    override suspend fun setEqBandLevel(index: Int, level: Int) {
        preferencesFlow.value = preferencesFlow.value.copy(
            eqBandLevels = preferencesFlow.value.eqBandLevels + (index to level)
        )
    }

    override suspend fun toggleFavoriteTrack(trackId: Long) {
        val current = preferencesFlow.value.favoriteTrackIds.toMutableSet()
        if (!current.add(trackId)) {
            current.remove(trackId)
        }
        preferencesFlow.value = preferencesFlow.value.copy(favoriteTrackIds = current)
    }

    override suspend fun recordRecentTrack(trackId: Long) {
        preferencesFlow.value = preferencesFlow.value.copy(
            recentTrackIds = (listOf(trackId) + preferencesFlow.value.recentTrackIds).distinct()
        )
    }

    override suspend fun setDarkModeMode(mode: DarkModeMode) {
        preferencesFlow.value = preferencesFlow.value.copy(darkModeMode = mode)
    }

    override suspend fun setAudioEffectsEnabled(enabled: Boolean) {
        preferencesFlow.value = preferencesFlow.value.copy(audioEffectsEnabled = enabled)
    }

    override suspend fun setAppLanguage(language: AppLanguage) {
        preferencesFlow.value = preferencesFlow.value.copy(appLanguage = language)
    }

    override suspend fun setManualLibraryFolder(uri: String?, label: String?) {
        preferencesFlow.value = preferencesFlow.value.copy(
            manualLibraryFolderUri = uri,
            manualLibraryFolderLabel = label
        )
    }

    fun emit(preferences: UserPreferences) {
        preferencesFlow.value = preferences
    }
}
