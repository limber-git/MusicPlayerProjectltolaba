package com.limbe.hexamusicplayer

import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.model.UserPreferences
import com.limbe.hexamusicplayer.domain.port.LocalMusicRepository
import com.limbe.hexamusicplayer.domain.port.UserPreferencesPort
import com.limbe.hexamusicplayer.domain.usecase.GetLocalTracksUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveUserPreferencesUseCase
import com.limbe.hexamusicplayer.ui.screens.library.LibraryViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

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
                    album = "Album A",
                    albumId = 10L,
                    durationMs = 120_000L,
                    contentUri = "content://song/1",
                    artworkUri = "content://albumart/10"
                )
            )
        )
        val preferences = FakeLibraryPreferencesPort()

        val viewModel = LibraryViewModel(
            GetLocalTracksUseCase(fakeRepository),
            ObserveUserPreferencesUseCase(preferences)
        )

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
        val preferences = FakeLibraryPreferencesPort()

        val viewModel = LibraryViewModel(
            GetLocalTracksUseCase(fakeRepository),
            ObserveUserPreferencesUseCase(preferences)
        )

        viewModel.refreshTracks()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.tracks.isEmpty())
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("Storage unavailable", viewModel.uiState.value.errorMessage)
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

private class FakeLibraryPreferencesPort : UserPreferencesPort {
    private val preferencesFlow = MutableStateFlow(UserPreferences())
    override val preferences: Flow<UserPreferences> = preferencesFlow

    override suspend fun setPlaybackSpeed(speed: Float) = Unit
    override suspend fun setPlaybackPitch(pitch: Float) = Unit
    override suspend fun setBassStrength(strength: Int) = Unit
    override suspend fun setVirtualizerStrength(strength: Int) = Unit
    override suspend fun setLoudnessGainMb(gainMb: Int) = Unit
    override suspend fun setEqBandLevel(index: Int, level: Int) = Unit
    override suspend fun toggleFavoriteTrack(trackId: Long) = Unit
    override suspend fun recordRecentTrack(trackId: Long) = Unit
    override suspend fun setDarkModeMode(mode: com.limbe.hexamusicplayer.domain.model.DarkModeMode) = Unit
    override suspend fun setAudioEffectsEnabled(enabled: Boolean) = Unit
}
